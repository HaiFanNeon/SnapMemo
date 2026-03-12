package com.example.snapmemo.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.snapmemo.R
import com.example.snapmemo.domain.model.Attachment

class AttachmentPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ivThumbnail: ImageView
    private val ivPlayIcon: ImageView
    private val tvFileName: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.item_attachment, this, true)
        ivThumbnail = findViewById(R.id.iv_thumbnail)
        ivPlayIcon = findViewById(R.id.iv_play_icon)
        tvFileName = findViewById(R.id.tv_file_name)
    }

    fun bind(attachment: Attachment, onPlayClick: ((Attachment) -> Unit)? = null) {
        val mime = attachment.mimeType
        when {
            mime.startsWith("image/") -> bindImage(attachment)
            mime.startsWith("video/") -> bindVideo(attachment, onPlayClick)
            mime.startsWith("audio/") -> bindAudio(attachment, onPlayClick)
            else -> bindFile(attachment)
        }
    }

    private fun bindImage(attachment: Attachment) {
        ivPlayIcon.visibility = View.GONE
        tvFileName.visibility = View.GONE
        val uri = attachment.localPath ?: attachment.remoteUrl
        if (uri != null) {
            ivThumbnail.load(uri) {
                crossfade(true)
                placeholder(R.drawable.ic_attachment)
                error(R.drawable.ic_attachment)
                transformations(RoundedCornersTransformation(8f))
            }
        } else {
            ivThumbnail.setImageResource(R.drawable.ic_attachment)
        }
    }

    private fun bindVideo(attachment: Attachment, onPlayClick: ((Attachment) -> Unit)?) {
        // 视频：显示封面（如有本地路径）+ 播放按钮
        ivPlayIcon.visibility = View.VISIBLE
        tvFileName.visibility = View.VISIBLE
        tvFileName.text = attachment.filename
        val uri = attachment.localPath ?: attachment.remoteUrl
        if (uri != null) {
            ivThumbnail.load(uri) {
                crossfade(true)
                placeholder(R.drawable.ic_attachment)
            }
        } else {
            ivThumbnail.setImageResource(R.drawable.ic_attachment)
        }
        ivPlayIcon.setOnClickListener { onPlayClick?.invoke(attachment) }
        setOnClickListener { onPlayClick?.invoke(attachment) }
    }

    private fun bindAudio(attachment: Attachment, onPlayClick: ((Attachment) -> Unit)?) {
        ivThumbnail.setImageResource(R.drawable.ic_attachment)
        ivPlayIcon.visibility = View.VISIBLE
        tvFileName.visibility = View.VISIBLE
        tvFileName.text = attachment.filename
        ivPlayIcon.setOnClickListener { onPlayClick?.invoke(attachment) }
        setOnClickListener { onPlayClick?.invoke(attachment) }
    }

    private fun bindFile(attachment: Attachment) {
        ivThumbnail.setImageResource(R.drawable.ic_attachment)
        ivPlayIcon.visibility = View.GONE
        tvFileName.visibility = View.VISIBLE
        tvFileName.text = attachment.filename
    }
}
