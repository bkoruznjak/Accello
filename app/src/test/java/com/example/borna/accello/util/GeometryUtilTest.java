package com.example.borna.accello.util;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by bkoruznjak on 09/04/2017.
 */

public class GeometryUtilTest {

    @Test
    public void distanceBetweenTwoPointsIs5() {
        assertThat(GeometryUtil.distanceBetweenTwoPoints(10, 10, 13, 14), is(5.0D));
    }

    @Test
    public void areCirclesIntersecting() {
        assertThat(GeometryUtil.areCirclesIntersecting(10, 10, 5, 13, 14, 1), is(true));
    }

    @Test
    public void areCirclesApart() {
        assertThat(GeometryUtil.areCirclesIntersecting(10, 10, 4, 13, 14, 1), is(false));
    }
}
