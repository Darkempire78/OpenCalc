package com.darkempire78.opencalculator.dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.darkempire78.opencalculator.R

class DonationDialog (
    private var context: Context,
    private var layoutInflater: LayoutInflater
) {

    fun openDonationDialog() {
        val dialog = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_donation, null)
        dialog.setView(dialogView)
        dialog.setTitle(context.getString(R.string.about_dialog_donation_title))

        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(15, 15, 15, 15)
        }
        dialogView.layoutParams = layoutParams

        val paypalImage = dialogView.findViewById<ImageView>(R.id.paypalImage)
        val bmacImage = dialogView.findViewById<ImageView>(R.id.bmacImage)
        val githubImage = dialogView.findViewById<ImageView>(R.id.githubImage)

        paypalImage.setOnClickListener {
            val paypalIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/ImDarkempire"))
            context.startActivity(paypalIntent)
        }

        bmacImage.setOnClickListener {
            val bmacIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buymeacoffee.com/darkempire78"))
            context.startActivity(bmacIntent)
        }

        githubImage.setOnClickListener {
            val bmacIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sponsors/Darkempire78"))
            context.startActivity(bmacIntent)
        }

        dialog.setPositiveButton(R.string.about_dialog_donation_close) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        dialog.show()
    }
}