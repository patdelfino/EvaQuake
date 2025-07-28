package com.example.evaquake

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OptionsBottomSheetDialogFragment : BottomSheetDialogFragment() {

    // Interface to communicate back to MainActivity
    interface OptionsListener {
        fun onOptionSelected(optionId: Int)
    }

    private var optionsListener: OptionsListener? = null

    // Companion object for option IDs
    companion object {
        const val OPTION_3D_MODEL = 1
        const val OPTION_AR_GUIDE = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_options_bottom_sheet, container, false)

        // Set up click listeners for the options
        view.findViewById<CardView>(R.id.option_3d_model).setOnClickListener {
            optionsListener?.onOptionSelected(OPTION_3D_MODEL)
            dismiss() // Dismiss the bottom sheet after selection
        }

        view.findViewById<CardView>(R.id.option_ar_guide).setOnClickListener {
            optionsListener?.onOptionSelected(OPTION_AR_GUIDE)
            dismiss() // Dismiss the bottom sheet after selection
        }

        return view
    }

    // Attach the listener when the fragment is attached to the activity
    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        if (context is OptionsListener) {
            optionsListener = context
        } else {
            throw RuntimeException("$context must implement OptionsListener")
        }
    }

    // Detach the listener when the fragment is detached
    override fun onDetach() {
        super.onDetach()
        optionsListener = null
    }
}
