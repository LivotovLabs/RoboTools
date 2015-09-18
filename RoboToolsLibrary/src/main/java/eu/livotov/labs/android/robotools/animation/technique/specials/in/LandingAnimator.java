package eu.livotov.labs.android.robotools.animation.technique.specials.in;

import android.animation.ObjectAnimator;
import android.view.View;

import eu.livotov.labs.android.robotools.animation.technique.BaseViewAnimator;
import eu.livotov.labs.android.robotools.animation.RTGlider;
import eu.livotov.labs.android.robotools.animation.skill.Skill;

public class LandingAnimator extends BaseViewAnimator{
    @Override
    protected void prepare(View target) {
        getAnimatorAgent().playTogether(
                RTGlider.glide(Skill.QuintEaseOut, getDuration(), ObjectAnimator.ofFloat(target, "scaleX", 1.5f, 1f)),
                RTGlider.glide(Skill.QuintEaseOut, getDuration(), ObjectAnimator.ofFloat(target, "scaleY", 1.5f, 1f)),
                RTGlider.glide(Skill.QuintEaseOut, getDuration(), ObjectAnimator.ofFloat(target, "alpha", 0, 1f))
        );
    }
}
