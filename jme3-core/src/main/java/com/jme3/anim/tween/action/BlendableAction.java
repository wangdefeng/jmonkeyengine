/*
 * Copyright (c) 2009-2022 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.anim.tween.action;

import com.jme3.anim.tween.AbstractTween;
import com.jme3.anim.tween.Tween;
import com.jme3.anim.util.HasLocalTransform;
import com.jme3.math.Transform;
import com.jme3.util.clone.Cloner;
import java.util.Collection;

public abstract class BlendableAction extends Action {

    protected BlendableAction collectTransformDelegate;
    private float transitionWeight = 1.0f;
    private double maxTransitionWeight = 1.0;
    private double transitionLength = 0.4f;
    private float weight = 1f;
    private TransitionTween transition = new TransitionTween(transitionLength);

    public BlendableAction(Tween... tweens) {
        super(tweens);
    }

    public void setCollectTransformDelegate(BlendableAction delegate) {
        this.collectTransformDelegate = delegate;
    }

    @Override
    public boolean interpolate(double t) {
        // Sanity check the inputs
        if (t < 0) {
            return true;
        }

        if (collectTransformDelegate == null) {
            if (transition.getLength() > getLength()) {
                transition.setLength(getLength());
            }
            if(isForward()) {
                transition.interpolate(t);
            } else {
                float v = Math.max((float)(getLength() - t), 0f);
                transition.interpolate(v);
            }
        } else {
            transitionWeight = 1f;
        }

        if (weight == 0) {
            //weight is 0 let's not interpolate
            return t < getLength();
        }

        doInterpolate(t);

        return t < getLength();
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    protected abstract void doInterpolate(double t);

    public abstract Collection<HasLocalTransform> getTargets();

    public abstract void collectTransform(HasLocalTransform target, Transform t, float weight, BlendableAction source);

    public double getTransitionLength() {
        return transitionLength;
    }

    public void setTransitionLength(double transitionLength) {
        this.transitionLength = transitionLength;
        this.transition.setLength(transitionLength);
    }

    protected float getTransitionWeight() {
        return transitionWeight;
    }

    /**
     * @param maxTransitionWeight The max transition weight. Must be &gt;=0 and &lt;=1 (default=1)
     * @throws IllegalArgumentException If maxTransitionWeight is not between 0 and 1.
     */
    public void setMaxTransitionWeight(double maxTransitionWeight) {
        if (maxTransitionWeight < 0.0 || maxTransitionWeight > 1.0) {
            throw new IllegalArgumentException("maxTransitionWeight must be between 0 and 1");
        }

        this.maxTransitionWeight = maxTransitionWeight;
    }

    /**
     *
     * @return The max transition weight (default=1)
     */
    public double getMaxTransitionWeight() {
        return maxTransitionWeight;
    }

    /**
     * Create a shallow clone for the JME cloner.
     *
     * @return a new action (not null)
     */
    @Override
    public BlendableAction jmeClone() {
        try {
            BlendableAction clone = (BlendableAction) super.clone();
            return clone;
        } catch (CloneNotSupportedException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Callback from {@link com.jme3.util.clone.Cloner} to convert this
     * shallow-cloned action into a deep-cloned one, using the specified cloner
     * and original to resolve copied fields.
     *
     * @param cloner the cloner that's cloning this action (not null)
     * @param original the action from which this action was shallow-cloned
     * (unused)
     */
    @Override
    public void cloneFields(Cloner cloner, Object original) {
        super.cloneFields(cloner, original);
        collectTransformDelegate = cloner.clone(collectTransformDelegate);
        transition = cloner.clone(transition);
    }

    private class TransitionTween extends AbstractTween {


        public TransitionTween(double length) {
            super(length);
        }

        @Override
        protected void doInterpolate(double t) {
            transitionWeight = (float) Math.min(t, maxTransitionWeight);
        }
    }

}
