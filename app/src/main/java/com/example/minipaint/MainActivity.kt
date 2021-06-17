package com.example.minipaint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         *  You will need to know the size of the view for drawing,
         *  but you cannot get the size of the view in the onCreate() method,
         *  because the size has not been determined at this point.
         * **/
        val myCanvasView = MyCanvasView(this)
        myCanvasView.fitsSystemWindows = true
        //myCanvasView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        myCanvasView.contentDescription = getString(R.string.canvasContentDescription)
        setContentView(myCanvasView)

    }
}