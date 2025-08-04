package com.example.evaquake

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.evaquake.databinding.FragmentOptionsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * A BottomSheetDialogFragment that displays options for 3D modeling and AR guide.
 * It uses a listener interface to communicate user selections back to the hosting Fragment.
 */
class OptionsBottomSheetDialogFragment : BottomSheetDialogFragment() {

    // View binding is used to access views in the layout
    private var _binding: FragmentOptionsBottomSheetBinding? = null
    private val binding get() = _binding!!

    // Listener to communicate selected option back to the parent Fragment
    private var optionsListener: OptionsListener? = null

    // Companion object to hold constant IDs for the options
    companion object {
        const val OPTION_3D_MODEL = 1
        const val OPTION_AR_GUIDE = 2
    }

    /**
     * Interface for the host Fragment to implement, allowing it to receive
     * callbacks when an option is selected.
     */
    interface OptionsListener {
        fun onOptionSelected(optionId: Int)
    }

    // This method is called to inflate the layout for the bottom sheet
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listener for the 3D Model card
        binding.option3dModel.setOnClickListener {
            // Notify the listener that the 3D model option was selected
            optionsListener?.onOptionSelected(OPTION_3D_MODEL)
            // Dismiss the bottom sheet after the option is selected
            dismiss()
        }

        // Set click listener for the AR Guide card
        binding.optionArGuide.setOnClickListener {
            // Notify the listener that the AR guide option was selected
            optionsListener?.onOptionSelected(OPTION_AR_GUIDE)
            // Dismiss the bottom sheet after the option is selected
            dismiss()
        }
    }

    // onAttach is called when the fragment is attached to its context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Check if the host Fragment implements the OptionsListener interface
        if (parentFragment is OptionsListener) {
            optionsListener = parentFragment as OptionsListener
        } else {
            // If the parent Fragment doesn't implement the interface, log a warning
            // instead of throwing an exception. This prevents the app from crashing.
            Log.e("OptionsBottomSheet", "Parent Fragment must implement OptionsListener")
        }
    }

    // onDetach is called when the fragment is no longer attached to its activity
    override fun onDetach() {
        super.onDetach()
        optionsListener = null
    }

    // This method is called when the fragment's view is being destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
