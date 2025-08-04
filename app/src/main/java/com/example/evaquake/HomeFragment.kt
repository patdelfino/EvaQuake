package com.example.evaquake

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.evaquake.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * A simple [Fragment] subclass.
 * This fragment serves as the main home screen of the application.
 */
class HomeFragment : Fragment(), OptionsBottomSheetDialogFragment.OptionsListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Declare a private property for Firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth instance
        firebaseAuth = FirebaseAuth.getInstance()

        // Get the current user
        val currentUser = firebaseAuth.currentUser

        // Check if a user is signed in and has a display name
        if (currentUser != null) {
            val userName = currentUser.displayName

            // Update the welcome text if a name is found, otherwise use a generic greeting
            if (!userName.isNullOrEmpty()) {
                binding.welcomeText.text = "Welcome, $userName!"
            } else {
                binding.welcomeText.text = "Welcome!"
            }
        } else {
            // Handle the case where no user is logged in
            binding.welcomeText.text = "Welcome!"
        }

        // Set click listeners for the main cards
        // Each listener now navigates to a new Activity.
        // You must create these new Activity files and declare them in AndroidManifest.xml.

        binding.cardSafetyGuides.setOnClickListener {
            // Intent to open the Safety Guides screen
            val intent = Intent(requireContext(), SafetyGuidesActivity::class.java)
            startActivity(intent)
        }

        binding.cardAboutEarthquakes.setOnClickListener {
            // Intent to open the About Earthquakes screen
            val intent = Intent(requireContext(), AboutEarthquakesActivity::class.java)
            startActivity(intent)
        }

        binding.cardEmergency.setOnClickListener {
            // Intent to open the Emergency Hotlines screen
            val intent = Intent(requireContext(), EmergencyHotlinesActivity::class.java)
            startActivity(intent)
        }

        binding.cardManageSchedule.setOnClickListener {
            // Intent to open the Manage Schedule screen
            val intent = Intent(requireContext(), ScheduleActivity::class.java)
            startActivity(intent)
        }

        binding.cardScanQr.setOnClickListener {
            // Intent to open the QR Scanner Activity directly
            val intent = Intent(requireContext(), QRScannerActivity::class.java)
            startActivity(intent)
        }

        // Set a click listener for the floating action buttons

        binding.fabOptions.setOnClickListener {
            // This button now opens the bottom sheet dialog for 3D/AR options
            val optionsBottomSheet = OptionsBottomSheetDialogFragment()
            optionsBottomSheet.show(childFragmentManager, optionsBottomSheet.tag)
        }

        binding.fabDrill.setOnClickListener {
            // This is still a placeholder, you can add more functionality later
            Toast.makeText(requireContext(), "Start Drill Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    // This method is part of the OptionsListener interface and is called when an option is selected from the bottom sheet
    override fun onOptionSelected(optionId: Int) {
        when (optionId) {
            OptionsBottomSheetDialogFragment.OPTION_3D_MODEL -> {
                // Intent to open the 3D Model viewer
                val intent = Intent(requireContext(), Building3DFragment::class.java)
                startActivity(intent)
            }
            OptionsBottomSheetDialogFragment.OPTION_AR_GUIDE -> {
                // Intent to open the AR Guide experience
                val intent = Intent(requireContext(), ARGuideFragment::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
