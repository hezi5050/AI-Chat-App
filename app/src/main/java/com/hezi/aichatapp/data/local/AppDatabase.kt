package com.hezi.aichatapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hezi.aichatapp.data.local.dao.ConversationDao
import com.hezi.aichatapp.data.local.dao.MessageDao
import com.hezi.aichatapp.data.local.entity.ConversationEntity
import com.hezi.aichatapp.data.local.entity.MessageEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}

