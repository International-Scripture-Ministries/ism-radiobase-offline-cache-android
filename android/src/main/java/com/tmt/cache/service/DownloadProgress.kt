package com.tmt.cache.service

data class DownloadProgress(
  val downloadId: Long,
  val bytesDownloaded: Long,
  val totalBytes: Long,
  val progressPercentage: Int,
  val progress: Long // Add this field to store the progress value
)
