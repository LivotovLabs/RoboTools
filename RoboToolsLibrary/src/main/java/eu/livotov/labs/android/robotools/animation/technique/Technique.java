
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 daimajia
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package eu.livotov.labs.android.robotools.animation.technique;

import eu.livotov.labs.android.robotools.animation.technique.attention.BounceAnimator;
import eu.livotov.labs.android.robotools.animation.technique.attention.FlashAnimator;
import eu.livotov.labs.android.robotools.animation.technique.attention.PulseAnimator;
import eu.livotov.labs.android.robotools.animation.technique.attention.RubberBandAnimator;
import eu.livotov.labs.android.robotools.animation.technique.attention.ShakeAnimator;
import eu.livotov.labs.android.robotools.animation.technique.attention.StandUpAnimator;
import eu.livotov.labs.android.robotools.animation.technique.attention.SwingAnimator;
import eu.livotov.labs.android.robotools.animation.technique.attention.TadaAnimator;
import eu.livotov.labs.android.robotools.animation.technique.attention.WaveAnimator;
import eu.livotov.labs.android.robotools.animation.technique.attention.WobbleAnimator;
import eu.livotov.labs.android.robotools.animation.technique.bouncing_entrances.BounceInAnimator;
import eu.livotov.labs.android.robotools.animation.technique.bouncing_entrances.BounceInDownAnimator;
import eu.livotov.labs.android.robotools.animation.technique.bouncing_entrances.BounceInLeftAnimator;
import eu.livotov.labs.android.robotools.animation.technique.bouncing_entrances.BounceInRightAnimator;
import eu.livotov.labs.android.robotools.animation.technique.bouncing_entrances.BounceInUpAnimator;
import eu.livotov.labs.android.robotools.animation.technique.fading_entrances.FadeInAnimator;
import eu.livotov.labs.android.robotools.animation.technique.fading_entrances.FadeInDownAnimator;
import eu.livotov.labs.android.robotools.animation.technique.fading_entrances.FadeInLeftAnimator;
import eu.livotov.labs.android.robotools.animation.technique.fading_entrances.FadeInRightAnimator;
import eu.livotov.labs.android.robotools.animation.technique.fading_entrances.FadeInUpAnimator;
import eu.livotov.labs.android.robotools.animation.technique.fading_exits.FadeOutAnimator;
import eu.livotov.labs.android.robotools.animation.technique.fading_exits.FadeOutDownAnimator;
import eu.livotov.labs.android.robotools.animation.technique.fading_exits.FadeOutLeftAnimator;
import eu.livotov.labs.android.robotools.animation.technique.fading_exits.FadeOutRightAnimator;
import eu.livotov.labs.android.robotools.animation.technique.fading_exits.FadeOutUpAnimator;
import eu.livotov.labs.android.robotools.animation.technique.flippers.FlipInXAnimator;
import eu.livotov.labs.android.robotools.animation.technique.flippers.FlipInYAnimator;
import eu.livotov.labs.android.robotools.animation.technique.flippers.FlipOutXAnimator;
import eu.livotov.labs.android.robotools.animation.technique.flippers.FlipOutYAnimator;
import eu.livotov.labs.android.robotools.animation.technique.rotating_entrances.RotateInAnimator;
import eu.livotov.labs.android.robotools.animation.technique.rotating_entrances.RotateInDownLeftAnimator;
import eu.livotov.labs.android.robotools.animation.technique.rotating_entrances.RotateInDownRightAnimator;
import eu.livotov.labs.android.robotools.animation.technique.rotating_entrances.RotateInUpLeftAnimator;
import eu.livotov.labs.android.robotools.animation.technique.rotating_entrances.RotateInUpRightAnimator;
import eu.livotov.labs.android.robotools.animation.technique.rotating_exits.RotateOutAnimator;
import eu.livotov.labs.android.robotools.animation.technique.rotating_exits.RotateOutDownLeftAnimator;
import eu.livotov.labs.android.robotools.animation.technique.rotating_exits.RotateOutDownRightAnimator;
import eu.livotov.labs.android.robotools.animation.technique.rotating_exits.RotateOutUpLeftAnimator;
import eu.livotov.labs.android.robotools.animation.technique.rotating_exits.RotateOutUpRightAnimator;
import eu.livotov.labs.android.robotools.animation.technique.sliders.SlideInDownAnimator;
import eu.livotov.labs.android.robotools.animation.technique.sliders.SlideInLeftAnimator;
import eu.livotov.labs.android.robotools.animation.technique.sliders.SlideInRightAnimator;
import eu.livotov.labs.android.robotools.animation.technique.sliders.SlideInUpAnimator;
import eu.livotov.labs.android.robotools.animation.technique.sliders.SlideOutDownAnimator;
import eu.livotov.labs.android.robotools.animation.technique.sliders.SlideOutLeftAnimator;
import eu.livotov.labs.android.robotools.animation.technique.sliders.SlideOutRightAnimator;
import eu.livotov.labs.android.robotools.animation.technique.sliders.SlideOutUpAnimator;
import eu.livotov.labs.android.robotools.animation.technique.specials.HingeAnimator;
import eu.livotov.labs.android.robotools.animation.technique.specials.RollInAnimator;
import eu.livotov.labs.android.robotools.animation.technique.specials.RollOutAnimator;
import eu.livotov.labs.android.robotools.animation.technique.specials.in.DropOutAnimator;
import eu.livotov.labs.android.robotools.animation.technique.specials.in.LandingAnimator;
import eu.livotov.labs.android.robotools.animation.technique.specials.out.TakingOffAnimator;
import eu.livotov.labs.android.robotools.animation.technique.zooming_entrances.ZoomInAnimator;
import eu.livotov.labs.android.robotools.animation.technique.zooming_entrances.ZoomInDownAnimator;
import eu.livotov.labs.android.robotools.animation.technique.zooming_entrances.ZoomInLeftAnimator;
import eu.livotov.labs.android.robotools.animation.technique.zooming_entrances.ZoomInRightAnimator;
import eu.livotov.labs.android.robotools.animation.technique.zooming_entrances.ZoomInUpAnimator;
import eu.livotov.labs.android.robotools.animation.technique.zooming_exits.ZoomOutAnimator;
import eu.livotov.labs.android.robotools.animation.technique.zooming_exits.ZoomOutDownAnimator;
import eu.livotov.labs.android.robotools.animation.technique.zooming_exits.ZoomOutLeftAnimator;
import eu.livotov.labs.android.robotools.animation.technique.zooming_exits.ZoomOutRightAnimator;
import eu.livotov.labs.android.robotools.animation.technique.zooming_exits.ZoomOutUpAnimator;

public enum Technique
{

    DropOut(DropOutAnimator.class),
    Landing(LandingAnimator.class),
    TakingOff(TakingOffAnimator.class),

    Flash(FlashAnimator.class),
    Pulse(PulseAnimator.class),
    RubberBand(RubberBandAnimator.class),
    Shake(ShakeAnimator.class),
    Swing(SwingAnimator.class),
    Wobble(WobbleAnimator.class),
    Bounce(BounceAnimator.class),
    Tada(TadaAnimator.class),
    StandUp(StandUpAnimator.class),
    Wave(WaveAnimator.class),

    Hinge(HingeAnimator.class),
    RollIn(RollInAnimator.class),
    RollOut(RollOutAnimator.class),

    BounceIn(BounceInAnimator.class),
    BounceInDown(BounceInDownAnimator.class),
    BounceInLeft(BounceInLeftAnimator.class),
    BounceInRight(BounceInRightAnimator.class),
    BounceInUp(BounceInUpAnimator.class),

    FadeIn(FadeInAnimator.class),
    FadeInUp(FadeInUpAnimator.class),
    FadeInDown(FadeInDownAnimator.class),
    FadeInLeft(FadeInLeftAnimator.class),
    FadeInRight(FadeInRightAnimator.class),

    FadeOut(FadeOutAnimator.class),
    FadeOutDown(FadeOutDownAnimator.class),
    FadeOutLeft(FadeOutLeftAnimator.class),
    FadeOutRight(FadeOutRightAnimator.class),
    FadeOutUp(FadeOutUpAnimator.class),

    FlipInX(FlipInXAnimator.class),
    FlipOutX(FlipOutXAnimator.class),
    FlipInY(FlipInYAnimator.class),
    FlipOutY(FlipOutYAnimator.class),
    RotateIn(RotateInAnimator.class),
    RotateInDownLeft(RotateInDownLeftAnimator.class),
    RotateInDownRight(RotateInDownRightAnimator.class),
    RotateInUpLeft(RotateInUpLeftAnimator.class),
    RotateInUpRight(RotateInUpRightAnimator.class),

    RotateOut(RotateOutAnimator.class),
    RotateOutDownLeft(RotateOutDownLeftAnimator.class),
    RotateOutDownRight(RotateOutDownRightAnimator.class),
    RotateOutUpLeft(RotateOutUpLeftAnimator.class),
    RotateOutUpRight(RotateOutUpRightAnimator.class),

    SlideInLeft(SlideInLeftAnimator.class),
    SlideInRight(SlideInRightAnimator.class),
    SlideInUp(SlideInUpAnimator.class),
    SlideInDown(SlideInDownAnimator.class),

    SlideOutLeft(SlideOutLeftAnimator.class),
    SlideOutRight(SlideOutRightAnimator.class),
    SlideOutUp(SlideOutUpAnimator.class),
    SlideOutDown(SlideOutDownAnimator.class),

    ZoomIn(ZoomInAnimator.class),
    ZoomInDown(ZoomInDownAnimator.class),
    ZoomInLeft(ZoomInLeftAnimator.class),
    ZoomInRight(ZoomInRightAnimator.class),
    ZoomInUp(ZoomInUpAnimator.class),

    ZoomOut(ZoomOutAnimator.class),
    ZoomOutDown(ZoomOutDownAnimator.class),
    ZoomOutLeft(ZoomOutLeftAnimator.class),
    ZoomOutRight(ZoomOutRightAnimator.class),
    ZoomOutUp(ZoomOutUpAnimator.class);



    private Class animatorClazz;

    private Technique(Class clazz) {
        animatorClazz = clazz;
    }

    public BaseViewAnimator getAnimator() {
        try {
            return (BaseViewAnimator) animatorClazz.newInstance();
        } catch (Exception e) {
            throw new Error("Can not init animatorClazz instance");
        }
    }
}
