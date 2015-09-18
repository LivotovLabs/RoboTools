package eu.livotov.labs.android.robotools.animation.technique.specials.out;

import android.animation.ObjectAnimator;
import android.view.View;

import eu.livotov.labs.android.robotools.animation.technique.BaseViewAnimator;
import eu.livotov.labs.android.robotools.animation.RTGlider;
import eu.livotov.labs.android.robotools.animation.skill.Skill;

public class TakingOffAnimator extends BaseViewAnimator {
    @Override
    protected void prepare(View target) {
        getAnimatorAgent().playTogether(
                RTGlider.glide(Skill.QuintEaseOut, getDuration(), ObjectAnimator.ofFloat(target, "scaleX", 1f, 1.5f)),
                RTGlider.glide(Skill.QuintEaseOut, getDuration(), ObjectAnimator.ofFloat(target, "scaleY", 1f, 1.5f)),
                RTGlider.glide(Skill.QuintEaseOut, getDuration(), ObjectAnimator.ofFloat(target, "alpha", 1, 0))
        );
    }
}
