package ab.melodiesPort.client.animation.animators;

public class TrumpetAnimator extends FluteAnimator {
    @Override
    protected float getVerticalOffset(float time) {
        return (float) (Math.cos(time * 0.05f) * 0.05f);
    }

    @Override
    protected float getHorizontalOffset(float time) {
        return (float) (Math.sin(time * 0.1f) * 0.05f) + 0.15f;
    }
}
