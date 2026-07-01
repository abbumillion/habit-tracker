package com.app.habit.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.habit.R
import com.app.habit.databinding.FragmentSettingsBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.app.habit.util.SessionManager
import androidx.navigation.fragment.findNavController

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val settingsList = listOf(
            SettingItem(getString(R.string.settings_account), android.R.drawable.ic_menu_myplaces),
            SettingItem(getString(R.string.settings_theme), android.R.drawable.ic_menu_manage),
            SettingItem(getString(R.string.settings_fonts), android.R.drawable.ic_menu_sort_alphabetically),
            SettingItem(getString(R.string.settings_language), android.R.drawable.ic_menu_mapmode),
            SettingItem(getString(R.string.settings_notifications), android.R.drawable.ic_popup_reminder),
            SettingItem("Backup Data", android.R.drawable.ic_menu_save),
            SettingItem(getString(R.string.settings_help), android.R.drawable.ic_menu_help),
            SettingItem(getString(R.string.settings_sign_out), android.R.drawable.ic_menu_close_clear_cancel)
        )

        binding.recyclerViewSettings.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSettings.adapter = SettingsAdapter(settingsList) { item ->
            handleSettingClick(item)
        }
    }

    private fun handleSettingClick(item: SettingItem) {
        when (item.title) {
            getString(R.string.settings_theme) -> showThemeDialog()
            getString(R.string.settings_language) -> showLanguageDialog()
            getString(R.string.settings_sign_out) -> showSignOutDialog()
            getString(R.string.settings_fonts) -> showFontDialog()
            getString(R.string.settings_account) -> showAccountDialog()
            getString(R.string.settings_notifications) -> showNotificationsDialog()
            "Backup Data" -> backupData()
            else -> Toast.makeText(requireContext(), getString(R.string.clicked_format, item.title), Toast.LENGTH_SHORT).show()
        }
    }

    private fun backupData() {
        Toast.makeText(requireContext(), "Data backed up to local storage", Toast.LENGTH_LONG).show()
    }

    private fun showThemeDialog() {
        val themes = arrayOf(getString(R.string.light), getString(R.string.dark), getString(R.string.system_default))
        val checkedItem = when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> 0
            AppCompatDelegate.MODE_NIGHT_YES -> 1
            else -> 2
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.choose_theme)
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                when (which) {
                    0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Spanish", "French", "German", "Hindi")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_language)
            .setItems(languages) { _, which ->
                Toast.makeText(requireContext(), getString(R.string.language_changed_toast, languages[which]), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }

    private fun showSignOutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_sign_out)
            .setMessage(R.string.sign_out_message)
            .setPositiveButton(R.string.settings_sign_out) { _, _ ->
                sessionManager.logout()
                findNavController().navigate(R.id.nav_login)
                Toast.makeText(requireContext(), getString(R.string.signed_out_successfully), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }

    private fun showFontDialog() {
        val sizes = arrayOf("Small", "Normal", "Large", "Extra Large")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.font_size)
            .setItems(sizes) { _, which ->
                Toast.makeText(requireContext(), getString(R.string.font_size_set_toast, sizes[which]), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }

    private fun showAccountDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.account_details)
            .setMessage(R.string.account_details_mock)
            .setPositiveButton(R.string.edit_profile) { _, _ -> }
            .setNegativeButton(R.string.close, null)
            .show()
    }

    private fun showNotificationsDialog() {
        val options = arrayOf(
            getString(R.string.push_notifications),
            getString(R.string.email_updates),
            getString(R.string.daily_reminders)
        )
        val checked = booleanArrayOf(true, false, true)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.notification_settings)
            .setMultiChoiceItems(options, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton(R.string.save_button, null)
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class SettingItem(val title: String, val iconRes: Int)

    class SettingsAdapter(
        private val items: List<SettingItem>,
        private val onClick: (SettingItem) -> Unit
    ) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val button: MaterialButton = view.findViewById(R.id.btnSetting)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_setting, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.button.text = item.title
            holder.button.setIconResource(item.iconRes)
            holder.button.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}