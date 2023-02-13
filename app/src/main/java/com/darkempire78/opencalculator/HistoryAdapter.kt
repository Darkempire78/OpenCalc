package com.darkempire78.opencalculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private var history: MutableList<History>,
    private val onElementClick: (value: String) -> Unit,
    ) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.history_item, parent, false)
            return HistoryViewHolder(view)
        }

        override fun getItemCount(): Int = history.size

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            holder.bind(history[position])
        }

        fun appendHistory(historyList: List<History>) {
            this.history.addAll(historyList)
            notifyItemRangeInserted(
                this.history.size,
                historyList.size - 1
            )
        }

        fun appendOneHistoryElement(history: History) {
            this.history.add(history)
            notifyDataSetChanged()
        }

        fun clearHistory() {
            this.history.clear()
            notifyDataSetChanged()
        }

        inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val calculation: TextView = itemView.findViewById(R.id.history_item_calculation)
            private val result: TextView = itemView.findViewById(R.id.history_item_result)
            private val time: TextView = itemView.findViewById(R.id.history_time)

            fun bind(history: History) {

                // Set calculation & result
                calculation.text = history.calculation
                result.text = history.result
                time.text = DateUtils.getRelativeTimeSpanString(
                    history.time.toLong(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE,
                )

                // On click
                calculation.setOnClickListener {
                    onElementClick.invoke(history.calculation)
                }
                result.setOnClickListener {
                    onElementClick.invoke(history.result)
                }
                calculation.setOnLongClickListener {
                    val clipboardManager = itemView.context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied history calculation", history.calculation)
                    clipboardManager.setPrimaryClip(clip)
                    Toast.makeText(itemView.context, R.string.value_copied, Toast.LENGTH_SHORT).show()
                    true // Or false if not consumed
                }
                result.setOnLongClickListener {
                    val clipboardManager = itemView.context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied history result", history.result)
                    clipboardManager.setPrimaryClip(clip)
                    Toast.makeText(itemView.context, R.string.value_copied, Toast.LENGTH_SHORT).show()
                    true // Or false if not consumed
                }
            }
        }
    }