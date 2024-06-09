package ab.melodiesPort.util;

import ab.melodiesPort.Common;
import ab.melodiesPort.resources.Melody;
import ab.melodiesPort.resources.Note;

import javax.sound.midi.*;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MidiParser {
    public static Melody parseMidi(InputStream inputStream, String midiName) {
        Melody melody = new Melody(midiName);
        try {
            Sequence sequence = MidiSystem.getSequence(inputStream);

            // Fetch shared events
            List<MidiEvent> sharedEvents = new LinkedList<>();
            for (Track track : sequence.getTracks()) {
                getEvents(track).stream()
                        .filter(event -> event.getMessage() instanceof MetaMessage m && m.getType() == 0x51)
                        .forEach(sharedEvents::add);
            }

            // Iterate through tracks and MIDI events
            int trackNr = 1;
            for (Track track : sequence.getTracks()) {
                // Merge with shared events and sort
                List<MidiEvent> events = getEvents(track);
                events.addAll(0, sharedEvents);
                events.sort((a, b) -> (int) (a.getTick() - b.getTick()));

                double bpm = 120;
                long lastTick = 0;
                double lastMs = 0;
                String name = "Track " + trackNr;
                List<Note> notes = new LinkedList<>();
                HashMap<Integer, Note.Builder> currentNotes = new HashMap<>();

                for (MidiEvent event : events) {
                    MidiMessage message = event.getMessage();

                    // Parse meta events
                    if (message instanceof MetaMessage metaMessage) {
                        byte[] data = metaMessage.getData();
                        int type = metaMessage.getType();
                        if (type == 0x03) {
                            name = new String(data).strip();
                        } else if (type == 0x04) {
                            String instrument = new String(data).strip();
                        } else if (type == 0x51) {
                            int microsecondsPerBeat = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                            bpm = Math.round(60000000.0f / microsecondsPerBeat);
                        }
                    }

                    // Parse note on/off events
                    if (message instanceof ShortMessage sm) {
                        int command = sm.getCommand();

                        // Convert notes into ms
                        long tick = event.getTick();
                        double deltaMs = ((tick - lastTick) * 60000.0) / (sequence.getResolution() * bpm);
                        double rms = (int) (deltaMs + lastMs);
                        lastTick = tick;
                        lastMs = rms;
                        int ms = (int) rms;

                        // Another way to decode note offs are note ons with velocity 0
                        if (command == ShortMessage.NOTE_ON && sm.getData2() == 0) {
                            command = ShortMessage.NOTE_OFF;
                        }

                        if (command == ShortMessage.NOTE_ON) {
                            int note = sm.getData1();
                            int velocity = sm.getData2();

                            // We simulate the (minimum) sustain as the time between releasing the key and pressing it again
                            if (currentNotes.containsKey(note)) {
                                Note.Builder previousNote = currentNotes.get(note);
                                previousNote.sustain = ms - previousNote.time;
                            }

                            currentNotes.put(note, new Note.Builder(note, velocity, ms));
                        } else if (command == ShortMessage.NOTE_OFF) {
                            int note = sm.getData1();
                            Note.Builder noteBuilder = currentNotes.get(note);
                            currentNotes.remove(note);
                            if (noteBuilder != null) {
                                noteBuilder.length = ms - noteBuilder.time;
                                notes.add(noteBuilder.build());
                            }
                        }
                    }
                }

                if (!notes.isEmpty()) {
                    trackNr += 1;

                    // Sort
                    notes.sort(Comparator.comparingInt(Note::getTime));

                    melody.addTrack(new ab.melodiesPort.resources.Track(name, notes));
                }
            }
        } catch (Exception e) {
            Common.LOGGER.error(e);
        }

        return melody;
    }

    private static List<MidiEvent> getEvents(Track track) {
        List<MidiEvent> events = new LinkedList<>();
        for (int i = 0; i < track.size(); i++) {
            events.add(track.get(i));
        }
        return events;
    }
}