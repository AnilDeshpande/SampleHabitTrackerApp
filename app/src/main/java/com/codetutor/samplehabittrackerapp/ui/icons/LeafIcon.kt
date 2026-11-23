package com.codetutor.samplehabittrackerapp.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** A sprout leaf: the first-habit and growth mark used on the empty overview. Tinted by the caller. */
val LeafIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Leaf",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(
            "M6.05 8.05c-2.73 2.73-2.73 7.15-.02 9.88 1.47-3.4 4.09-6.24 7.36-7.93-2.77 2.34-4.71 5.61-5.39 9.32 " +
                "2.6 1.23 5.8.78 7.95-1.37C19.43 14.47 20 4 20 4S9.53 3.43 6.05 8.05z",
        ).toNodes(),
        fill = SolidColor(Color.Black),
    ).build()
}
