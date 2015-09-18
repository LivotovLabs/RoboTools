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

package eu.livotov.labs.android.robotools.animation.skill;

import eu.livotov.labs.android.robotools.animation.skill.back.BackEaseIn;
import eu.livotov.labs.android.robotools.animation.skill.back.BackEaseInOut;
import eu.livotov.labs.android.robotools.animation.skill.back.BackEaseOut;
import eu.livotov.labs.android.robotools.animation.skill.bounce.BounceEaseIn;
import eu.livotov.labs.android.robotools.animation.skill.bounce.BounceEaseInOut;
import eu.livotov.labs.android.robotools.animation.skill.bounce.BounceEaseOut;
import eu.livotov.labs.android.robotools.animation.skill.circ.CircEaseIn;
import eu.livotov.labs.android.robotools.animation.skill.circ.CircEaseInOut;
import eu.livotov.labs.android.robotools.animation.skill.circ.CircEaseOut;
import eu.livotov.labs.android.robotools.animation.skill.cubic.CubicEaseIn;
import eu.livotov.labs.android.robotools.animation.skill.cubic.CubicEaseInOut;
import eu.livotov.labs.android.robotools.animation.skill.cubic.CubicEaseOut;
import eu.livotov.labs.android.robotools.animation.skill.elastic.ElasticEaseIn;
import eu.livotov.labs.android.robotools.animation.skill.elastic.ElasticEaseOut;
import eu.livotov.labs.android.robotools.animation.skill.expo.ExpoEaseIn;
import eu.livotov.labs.android.robotools.animation.skill.expo.ExpoEaseInOut;
import eu.livotov.labs.android.robotools.animation.skill.expo.ExpoEaseOut;
import eu.livotov.labs.android.robotools.animation.skill.quad.QuadEaseIn;
import eu.livotov.labs.android.robotools.animation.skill.quad.QuadEaseInOut;
import eu.livotov.labs.android.robotools.animation.skill.quad.QuadEaseOut;
import eu.livotov.labs.android.robotools.animation.skill.quint.QuintEaseIn;
import eu.livotov.labs.android.robotools.animation.skill.quint.QuintEaseInOut;
import eu.livotov.labs.android.robotools.animation.skill.quint.QuintEaseOut;
import eu.livotov.labs.android.robotools.animation.skill.sine.SineEaseIn;
import eu.livotov.labs.android.robotools.animation.skill.sine.SineEaseInOut;
import eu.livotov.labs.android.robotools.animation.skill.sine.SineEaseOut;
import eu.livotov.labs.android.robotools.animation.skill.linear.Linear;


public enum  Skill {

    BackEaseIn(BackEaseIn.class),
    BackEaseOut(BackEaseOut.class),
    BackEaseInOut(BackEaseInOut.class),

    BounceEaseIn(BounceEaseIn.class),
    BounceEaseOut(BounceEaseOut.class),
    BounceEaseInOut(BounceEaseInOut.class),

    CircEaseIn(CircEaseIn.class),
    CircEaseOut(CircEaseOut.class),
    CircEaseInOut(CircEaseInOut.class),

    CubicEaseIn(CubicEaseIn.class),
    CubicEaseOut(CubicEaseOut.class),
    CubicEaseInOut(CubicEaseInOut.class),

    ElasticEaseIn(ElasticEaseIn.class),
    ElasticEaseOut(ElasticEaseOut.class),

    ExpoEaseIn(ExpoEaseIn.class),
    ExpoEaseOut(ExpoEaseOut.class),
    ExpoEaseInOut(ExpoEaseInOut.class),

    QuadEaseIn(QuadEaseIn.class),
    QuadEaseOut(QuadEaseOut.class),
    QuadEaseInOut(QuadEaseInOut.class),

    QuintEaseIn(QuintEaseIn.class),
    QuintEaseOut(QuintEaseOut.class),
    QuintEaseInOut(QuintEaseInOut.class),

    SineEaseIn(SineEaseIn.class),
    SineEaseOut(SineEaseOut.class),
    SineEaseInOut(SineEaseInOut.class),

    Linear(Linear.class);


    private Class easingMethod;

    private Skill(Class clazz) {
        easingMethod = clazz;
    }

    public BaseEasingMethod getMethod(float duration) {
        try {
            return (BaseEasingMethod)easingMethod.getConstructor(float.class).newInstance(duration);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("Can not init easingMethod instance");
        }
    }
}
