package com.capstone.fruitsguard

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat

class MyEditTextPassword @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), View.OnTouchListener  {

    private var passwordToggleImage: Drawable
    private var isPasswordVisible = false

    init {
        passwordToggleImage = ContextCompat.getDrawable(context, R.drawable.baseline_closed_eye_24) as Drawable
        setOnTouchListener(this)
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //Do Nothing
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) showPasswordToggle() else hidePasswordToggle()
            }

            override fun afterTextChanged(s: Editable) {
                //Do Nothing
            }

        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        hint = "Masukkan Password"
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        textSize = 16f
    }

    private fun showPasswordToggle() {
        setButtonDrawables(endOfTheText = passwordToggleImage)
    }

    private fun hidePasswordToggle() {
        setButtonDrawables()
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordToggleImage = ContextCompat.getDrawable(context, R.drawable.baseline_remove_red_eye_24) as Drawable
        } else {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordToggleImage = ContextCompat.getDrawable(context, R.drawable.baseline_closed_eye_24) as Drawable
        }
        setSelection(text?.length ?: 0) // Agar kursor tetap di posisi akhir
        showPasswordToggle()
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        if (compoundDrawables[2] != null) {
            val toggleButtonStart: Float
            val toggleButtonEnd: Float
            var isToggleButtonClick = false
            if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                toggleButtonEnd = (passwordToggleImage.intrinsicWidth + paddingStart).toFloat()
                if (event.x < toggleButtonEnd) isToggleButtonClick = true
            } else {
                toggleButtonStart = (width - paddingEnd - passwordToggleImage.intrinsicWidth).toFloat()
                if (event.x > toggleButtonStart) isToggleButtonClick = true
            }
            if (isToggleButtonClick) {
                if (event.action == MotionEvent.ACTION_UP) {
                    togglePasswordVisibility()
                    return true
                }
            }
        }
        return false
    }

    private fun setButtonDrawables(
        startOfTheText: Drawable? = null,
        topOfTheText: Drawable? = null,
        endOfTheText: Drawable? = null,
        bottomOfTheText: Drawable? = null
    ) {
        setCompoundDrawablesWithIntrinsicBounds(
            startOfTheText,
            topOfTheText,
            endOfTheText,
            bottomOfTheText
        )
    }
}