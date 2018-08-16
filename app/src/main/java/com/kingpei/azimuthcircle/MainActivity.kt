package com.kingpei.azimuthcircle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.kingpei.customview.AzimuthCircle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun circlePressed(view: View){
        val azimuthCircle = view as AzimuthCircle
        if (azimuthCircle.pressDirection === AzimuthCircle.LEFT_PRESS) {
            Toast.makeText(this, "LEFT_PRESS", Toast.LENGTH_SHORT).show()
        } else if (azimuthCircle.pressDirection === AzimuthCircle.TOP_PRESS) {
            Toast.makeText(this, "TOP_PRESS", Toast.LENGTH_SHORT).show()
        } else if (azimuthCircle.pressDirection === AzimuthCircle.RIGHT_PRESS) {
            Toast.makeText(this, "RIGHT_PRESS", Toast.LENGTH_SHORT).show()
        } else if (azimuthCircle.pressDirection === AzimuthCircle.BOTTOM_PRESS) {
            Toast.makeText(this, "BOTTOM_PRESS", Toast.LENGTH_SHORT).show()
        } else {
        }
    }

    fun okClick(view :View){
        Toast.makeText(this, "OK_PRESS", Toast.LENGTH_SHORT).show()
    }
}
