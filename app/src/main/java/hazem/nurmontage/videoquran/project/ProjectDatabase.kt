package hazem.nurmontage.videoquran.project

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * Simple SQLite-based project storage (no Room dependency).
 * Provides CRUD operations for projects.
 */
class ProjectDatabase(context: Context) {

    /**
     * Represents a project record.
     */
    class ProjectRecord {
        var id: Long = 0
        var name: String? = null
        var createdAt: Long = 0
        var updatedAt: Long = 0
        var width: Int = 0
        var height: Int = 0
        var data: String? = null // JSON
    }

    private val dbHelper: DatabaseHelper = DatabaseHelper(context.applicationContext)

    private class DatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            val createTable = "CREATE TABLE $TABLE_PROJECTS (" +
                    "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COL_NAME TEXT NOT NULL, " +
                    "$COL_CREATED_AT INTEGER NOT NULL, " +
                    "$COL_UPDATED_AT INTEGER NOT NULL, " +
                    "$COL_WIDTH INTEGER DEFAULT 1080, " +
                    "$COL_HEIGHT INTEGER DEFAULT 1920, " +
                    "$COL_DATA TEXT" +
                    ")"
            db.execSQL(createTable)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_PROJECTS")
            onCreate(db)
        }
    }

    /**
     * Insert a new project.
     * @return the row ID of the newly inserted row, or -1 on error
     */
    fun insertProject(project: ProjectRecord): Long {
        val db = dbHelper.writableDatabase
        try {
            val now = System.currentTimeMillis()
            val values = ContentValues().apply {
                put(COL_NAME, project.name)
                put(COL_CREATED_AT, now)
                put(COL_UPDATED_AT, now)
                put(COL_WIDTH, project.width)
                put(COL_HEIGHT, project.height)
                put(COL_DATA, project.data)
            }

            val id = db.insert(TABLE_PROJECTS, null, values)
            if (id == -1L) {
                Log.e(TAG, "Failed to insert project")
            }
            return id
        } finally {
            db.close()
        }
    }

    /**
     * Get a project by ID.
     */
    fun getProject(id: Long): ProjectRecord? {
        val db = dbHelper.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                TABLE_PROJECTS, null,
                "$COL_ID = ?", arrayOf(id.toString()),
                null, null, null
            )

            if (cursor != null && cursor.moveToFirst()) {
                return cursorToRecord(cursor)
            }
            return null
        } finally {
            cursor?.close()
            db.close()
        }
    }

    /**
     * Get all projects, ordered by most recently updated.
     */
    fun getAllProjects(): List<ProjectRecord> {
        val projects = mutableListOf<ProjectRecord>()
        val db = dbHelper.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                TABLE_PROJECTS, null, null, null,
                null, null, "$COL_UPDATED_AT DESC"
            )

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    projects.add(cursorToRecord(cursor))
                }
            }
        } finally {
            cursor?.close()
            db.close()
        }
        return projects
    }

    /**
     * Update an existing project.
     * @return the number of rows affected
     */
    fun updateProject(project: ProjectRecord): Int {
        val db = dbHelper.writableDatabase
        try {
            val values = ContentValues().apply {
                put(COL_NAME, project.name)
                put(COL_UPDATED_AT, System.currentTimeMillis())
                put(COL_WIDTH, project.width)
                put(COL_HEIGHT, project.height)
                put(COL_DATA, project.data)
            }

            return db.update(
                TABLE_PROJECTS, values,
                "$COL_ID = ?", arrayOf(project.id.toString())
            )
        } finally {
            db.close()
        }
    }

    /**
     * Delete a project by ID.
     * @return the number of rows affected
     */
    fun deleteProject(id: Long): Int {
        val db = dbHelper.writableDatabase
        try {
            return db.delete(
                TABLE_PROJECTS,
                "$COL_ID = ?", arrayOf(id.toString())
            )
        } finally {
            db.close()
        }
    }

    /**
     * Get the count of projects.
     */
    fun getProjectCount(): Int {
        val db = dbHelper.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_PROJECTS", null)
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
            return 0
        } finally {
            cursor?.close()
            db.close()
        }
    }

    /**
     * Search projects by name.
     */
    fun searchProjects(query: String): List<ProjectRecord> {
        val projects = mutableListOf<ProjectRecord>()
        val db = dbHelper.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                TABLE_PROJECTS, null,
                "$COL_NAME LIKE ?", arrayOf("%$query%"),
                null, null, "$COL_UPDATED_AT DESC"
            )

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    projects.add(cursorToRecord(cursor))
                }
            }
        } finally {
            cursor?.close()
            db.close()
        }
        return projects
    }

    private fun cursorToRecord(cursor: Cursor): ProjectRecord {
        val record = ProjectRecord()
        record.id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID))
        record.name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
        record.createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
        record.updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_UPDATED_AT))
        record.width = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WIDTH))
        record.height = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HEIGHT))
        record.data = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA))
        return record
    }

    /**
     * Close the database helper.
     */
    fun close() {
        dbHelper.close()
    }

    companion object {
        private const val TAG = "ProjectDatabase"
        private const val DB_NAME = "quran_projects.db"
        private const val DB_VERSION = 1

        private const val TABLE_PROJECTS = "projects"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_CREATED_AT = "created_at"
        private const val COL_UPDATED_AT = "updated_at"
        private const val COL_WIDTH = "width"
        private const val COL_HEIGHT = "height"
        private const val COL_DATA = "data"
    }
}
