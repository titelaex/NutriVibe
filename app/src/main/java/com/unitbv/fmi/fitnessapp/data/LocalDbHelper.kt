package com.unitbv.fmi.fitnessapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.unitbv.fmi.fitnessapp.models.Recipe

class LocalDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "nutrivibe_local.db"
        private const val DATABASE_VERSION = 3

        const val TABLE_RECIPES = "recipes"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CALORIES = "calories"
        const val COLUMN_PROTEIN = "protein"
        const val COLUMN_CARBS = "carbs"
        const val COLUMN_FATS = "fats"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_PREP_TIME = "prep_time"
        const val COLUMN_INSTRUCTIONS = "instructions"
        const val COLUMN_DIFFICULTY = "difficulty"

        const val TABLE_WORKOUTS = "workouts"
        const val COLUMN_WORKOUT_ID = "workout_id"
        const val COLUMN_WORKOUT_NAME = "workout_name"
        const val COLUMN_WORKOUT_DESC = "workout_desc"
        const val COLUMN_WORKOUT_CATEGORY = "workout_category"
        const val COLUMN_WORKOUT_DURATION = "workout_duration"
        const val COLUMN_WORKOUT_DIFFICULTY = "workout_difficulty"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_RECIPES (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT,
                $COLUMN_CALORIES INTEGER,
                $COLUMN_PROTEIN INTEGER,
                $COLUMN_CARBS INTEGER,
                $COLUMN_FATS INTEGER,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_PREP_TIME INTEGER,
                $COLUMN_INSTRUCTIONS TEXT,
                $COLUMN_DIFFICULTY TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)

        val createWorkoutsTable = """
            CREATE TABLE $TABLE_WORKOUTS (
                $COLUMN_WORKOUT_ID TEXT PRIMARY KEY,
                $COLUMN_WORKOUT_NAME TEXT,
                $COLUMN_WORKOUT_DESC TEXT,
                $COLUMN_WORKOUT_CATEGORY TEXT,
                $COLUMN_WORKOUT_DURATION INTEGER,
                $COLUMN_WORKOUT_DIFFICULTY TEXT
            )
        """.trimIndent()
        db.execSQL(createWorkoutsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECIPES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORKOUTS")
        onCreate(db)
    }

    fun insertRecipe(recipe: Recipe) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, recipe.id)
            put(COLUMN_NAME, recipe.name)
            put(COLUMN_CALORIES, recipe.calories)
            put(COLUMN_PROTEIN, recipe.protein)
            put(COLUMN_CARBS, recipe.carbs)
            put(COLUMN_FATS, recipe.fats)
            put(COLUMN_CATEGORY, recipe.category)
            put(COLUMN_PREP_TIME, recipe.prepTime)
            put(COLUMN_INSTRUCTIONS, recipe.instructions)
            put(COLUMN_DIFFICULTY, recipe.difficulty)
        }
        db.insertWithOnConflict(TABLE_RECIPES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllRecipes(): List<Recipe> {
        val list = mutableListOf<Recipe>()
        val db = readableDatabase
        val cursor = db.query(TABLE_RECIPES, null, null, null, null, null, null)
        with(cursor) {
            while (moveToNext()) {
                val id = getString(getColumnIndexOrThrow(COLUMN_ID))
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                val calories = getInt(getColumnIndexOrThrow(COLUMN_CALORIES))
                val protein = getInt(getColumnIndexOrThrow(COLUMN_PROTEIN))
                val carbs = getInt(getColumnIndexOrThrow(COLUMN_CARBS))
                val fats = getInt(getColumnIndexOrThrow(COLUMN_FATS))
                val category = getString(getColumnIndexOrThrow(COLUMN_CATEGORY))
                val prepTime = getInt(getColumnIndexOrThrow(COLUMN_PREP_TIME))
                val instructions = getString(getColumnIndexOrThrow(COLUMN_INSTRUCTIONS))
                val difficulty = getString(getColumnIndexOrThrow(COLUMN_DIFFICULTY))

                list.add(
                    Recipe(
                        id = id,
                        name = name,
                        calories = calories,
                        protein = protein,
                        carbs = carbs,
                        fats = fats,
                        category = category,
                        ingredients = emptyList(),
                        instructions = instructions,
                        difficulty = difficulty,
                        prepTime = prepTime
                    )
                )
            }
            close()
        }
        return list
    }

    fun clearRecipes() {
        val db = writableDatabase
        db.delete(TABLE_RECIPES, null, null)
    }

    fun insertWorkout(workout: com.unitbv.fmi.fitnessapp.models.Workout) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_WORKOUT_ID, workout.id)
            put(COLUMN_WORKOUT_NAME, workout.name)
            put(COLUMN_WORKOUT_DESC, workout.description)
            put(COLUMN_WORKOUT_CATEGORY, workout.category)
            put(COLUMN_WORKOUT_DURATION, workout.durationMin)
            put(COLUMN_WORKOUT_DIFFICULTY, workout.difficulty)
        }
        db.insertWithOnConflict(TABLE_WORKOUTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllWorkouts(): List<com.unitbv.fmi.fitnessapp.models.Workout> {
        val list = mutableListOf<com.unitbv.fmi.fitnessapp.models.Workout>()
        val db = readableDatabase
        val cursor = db.query(TABLE_WORKOUTS, null, null, null, null, null, null)
        with(cursor) {
            while (moveToNext()) {
                val id = getString(getColumnIndexOrThrow(COLUMN_WORKOUT_ID))
                val name = getString(getColumnIndexOrThrow(COLUMN_WORKOUT_NAME))
                val desc = getString(getColumnIndexOrThrow(COLUMN_WORKOUT_DESC))
                val category = getString(getColumnIndexOrThrow(COLUMN_WORKOUT_CATEGORY))
                val duration = getInt(getColumnIndexOrThrow(COLUMN_WORKOUT_DURATION))
                val diff = getString(getColumnIndexOrThrow(COLUMN_WORKOUT_DIFFICULTY))
                
                list.add(com.unitbv.fmi.fitnessapp.models.Workout(id, name, desc, category, duration, diff))
            }
            close()
        }
        return list
    }

    fun clearWorkouts() {
        val db = writableDatabase
        db.delete(TABLE_WORKOUTS, null, null)
    }
}
