/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015 Oliver Lum
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package oarlib.display.util;

import java.awt.*;

/**
 * Created by oliverlum on 9/27/14.
 */
public class Colors {
    public static final int VIVID_YELLOW = 0xFFB300; // Vivid Yellow
    public static final int STRONG_PURPLE = 0x803E75; // Strong Purple
    // The following don't work well for people with defective color vision
    public static final int VIVID_GREEN = 0x007D34; // Vivid Green
    public static final int STRONG_PURPLISH_PINK = 0xF6768E; // Strong Purplish Pink
    public static final int STRONG_BLUE = 0x00538A; // Strong Blue
    public static final int STRONG_YELLOWISH_PINK = 0xFF7A5C; // Strong Yellowish Pink
    public static final int STRONG_VIOLET = 0x53377A; // Strong Violet
    public static final int VIVID_ORANGE_YELLOW = 0xFF8E00;  // Vivid Orange Yellow
    public static final int STRONG_PURPLISH_RED = 0xB32851; // Strong Purplish Red
    public static final int VIVID_GREENISH_YELLOW = 0xF4C800; // Vivid Greenish Yellow
    public static final int STRONG_REDDISH_BROWN = 0x7F180D; // Strong Reddish Brown
    public static final int VIVID_YELLOWISH_GREEN = 0x93AA00; // Vivid Yellowish Green
    public static final int DEEP_YELLOWISH_BROWN = 0x593315; // Deep Yellowish Brown
    public static final int VIVID_REDDISH_ORANGE = 0xF13A13; // Vivid Reddish Orange
    public static final int DARK_OLIVE_GREEN = 0x232C16; // Dark Olive Green
    public static final int VIVID_ORANGE = 0xFF6800; // Vivid Orange
    public static final int VERY_LIGHT_BLUE = 0xA6BDD7; // Very Light Blue
    public static final Color[] RYGCBGB = new Color[]{
            Color.RED,
            new Color(VIVID_YELLOW),
            Color.GREEN,
            Color.BLUE,
            new Color(VERY_LIGHT_BLUE),
            Color.BLACK
    };
    public static final int VIVID_RED = 0xC10020; // Vivid Red
    public static final int GRAYISH_YELLOW = 0xCEA262; // Grayish Yellow
    public static final int MEDIUM_GRAY = 0x817066; // Medium Gray
    public static final Color[] RESTRICTED_KELLY_COLORS = new Color[]{
            new Color(VIVID_YELLOW),
            new Color(STRONG_PURPLE),
            new Color(VIVID_ORANGE),
            new Color(VERY_LIGHT_BLUE),
            new Color(VIVID_RED),
            new Color(GRAYISH_YELLOW),
            new Color(MEDIUM_GRAY),
    };
    public static final Color[] KELLY_COLORS = new Color[]{
            new Color(VIVID_YELLOW),
            new Color(STRONG_PURPLE),
            new Color(VIVID_ORANGE),
            new Color(VERY_LIGHT_BLUE),
            new Color(VIVID_RED),
            new Color(GRAYISH_YELLOW),
            new Color(MEDIUM_GRAY),
            new Color(VIVID_GREEN),
            new Color(STRONG_PURPLISH_PINK),
            new Color(STRONG_BLUE),
            new Color(STRONG_YELLOWISH_PINK),
            new Color(STRONG_VIOLET),
            new Color(VIVID_ORANGE_YELLOW),
            new Color(STRONG_PURPLISH_RED),
            new Color(VIVID_GREENISH_YELLOW),
            new Color(STRONG_REDDISH_BROWN),
            new Color(VIVID_YELLOWISH_GREEN),
            new Color(DEEP_YELLOWISH_BROWN),
            new Color(VIVID_REDDISH_ORANGE),
            new Color(DARK_OLIVE_GREEN)
    };
    public static final int INDIGO = 0x6F00FF;
    public static final int VIOLET = 0x663399;

}
