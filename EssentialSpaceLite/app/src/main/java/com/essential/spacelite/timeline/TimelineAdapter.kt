package com.essential.spacelite.timeline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.essential.spacelite.R
import com.essential.spacelite.data.entity.CaptureEntry
import com.essential.spacelite.databinding.ItemTimelineEntryBinding
import com.essential.spacelite.utils.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TimelineAdapter(
    private val onItemClick: (CaptureEntry) -> Unit,
    private val onItemLongClick: (CaptureEntry) -> Boolean
) : ListAdapter<CaptureEntry, TimelineAdapter.EntryViewHolder>(DiffCallback) {

    private val dateFormat = SimpleDateFormat("MMM d", Locale.US)
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
    private val headerFormat = SimpleDateFormat("EEEE, MMMM d", Locale.US)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemTimelineEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = getItem(position)
        val prevEntry = if (position > 0) getItem(position - 1) else null
        holder.bind(entry, prevEntry)
    }

    inner class EntryViewHolder(
        private val binding: ItemTimelineEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: CaptureEntry, prevEntry: CaptureEntry?) {
            // Date header: show if different day from previous
            val entryDay = dateFormat.format(Date(entry.timestamp))
            val prevDay = prevEntry?.let { dateFormat.format(Date(it.timestamp)) }
            if (entryDay != prevDay) {
                binding.dateHeader.visibility = View.VISIBLE
                binding.dateHeader.text = headerFormat.format(Date(entry.timestamp))
            } else {
                binding.dateHeader.visibility = View.GONE
            }

            // Timestamp
            binding.entryTime.text = timeFormat.format(Date(entry.timestamp))

            // Thumbnail
            Glide.with(binding.root)
                .load(File(entry.thumbnailPath))
                .centerCrop()
                .placeholder(R.drawable.bg_thumbnail_placeholder)
                .into(binding.thumbnail)

            // Text note
            if (!entry.textNote.isNullOrBlank()) {
                binding.textNote.visibility = View.VISIBLE
                binding.textNote.text = entry.textNote
            } else {
                binding.textNote.visibility = View.GONE
            }

            // Voice badge
            if (entry.voiceNotePath != null && File(entry.voiceNotePath).exists()) {
                binding.voiceBadge.visibility = View.VISIBLE
                binding.voiceDuration.text = FileUtils.formatDuration(entry.voiceNoteDurationMs)
            } else {
                binding.voiceBadge.visibility = View.GONE
            }

            binding.root.setOnClickListener { onItemClick(entry) }
            binding.root.setOnLongClickListener { onItemLongClick(entry) }
        }
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<CaptureEntry>() {
            override fun areItemsTheSame(old: CaptureEntry, new: CaptureEntry) = old.id == new.id
            override fun areContentsTheSame(old: CaptureEntry, new: CaptureEntry) = old == new
        }
    }
}
