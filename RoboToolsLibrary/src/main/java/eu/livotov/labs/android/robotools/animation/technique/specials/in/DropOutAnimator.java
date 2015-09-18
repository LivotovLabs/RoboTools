package eu.livotov.labs.android.robotools.animation.technique.specials.in;

import android.animation.ObjectAnimator;
import android.view.View;

import eu.livotov.labs.android.robotools.animation.technique.BaseViewAnimator;
import eu.livotov.labs.android.robotools.animation.RTGlider;
import eu.livotov.labs.android.robotools.animation.skill.Skill;

public class DropOutAnimator extends BaseViewAnimator{
    @Override
    protected void prepare(View target) {
        int distance = target.getTop() + target.getHeight();
        getAnimatorAgent().playTogether(
                ObjectAnimator.ofFloat(target, "alpha", 0, 1),
                RTGlider.glide(Skill.BounceEaseOut, getDuration(), ObjectAnimator.ofFloat(target, "translationY", -distance, 0))
        );
    }
}
