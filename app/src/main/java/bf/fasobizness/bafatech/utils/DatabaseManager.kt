package bf.fasobizness.bafatech.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import bf.fasobizness.bafatech.models.Message
import bf.fasobizness.bafatech.models.Recruit.Recrutement
import bf.fasobizness.bafatech.models.User
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class DatabaseManager(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val query_recrutement = ("CREATE TABLE recrutements ("
                + " id_recr integer PRIMARY KEY NOT NULL,"
                + " nom_ent varchar(255),"
                + " domaine varchar(255),"
                + " description text,"
                + " descr varchar(255),"
                + " date_pub varchar(255),"
                + " date_fin date,"
                + " heure_fin time,"
                + " nom_r varchar(255),"
                + " vue varchar(255),"
                + " lien varchar(255),"
                + " share varchar(255)"
                + ")")
        db.execSQL(query_recrutement)
        val query_recruit_attachment = ("CREATE TABLE affiche_rec ("
                + " id_recr integer PRIMARY KEY autoincrement,"
                + " nom varchar(255),"
                + " thumbnail varchar(255),"
                + " id_rec_fk integer"
                + ")")
        db.execSQL(query_recruit_attachment)
        val query_messages = ("CREATE TABLE messages ("
                + " message_id integer PRIMARY KEY autoincrement,"
                + " message text,"
                + " created_at varchar(255),"
                + " etat integer,"
                + " discussion_id integer,"
                + " isread integer,"
                + " type varchar(255),"
                + " is_deleted integer,"
                + " sender integer,"
                + " annonce integer,"
                + " titre varchar(255),"
                + " affiche varchar(255),"
                + " id_ann interger,"
                + " user varchar(255)"
                + ")")
        db.execSQL(query_messages)
        val query_message = ("CREATE TABLE message ("
                + " discussion_id integer PRIMARY KEY autoincrement,"
                + " user text,"
                + " id_ann integer,"
                + " annonce integer,"
                + " titre varchar(255),"
                + " affiche varchar(255)"
                + ")")
        db.execSQL(query_message)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val query_recrutement = "DROP TABLE recrutements"
        val query_recrutement_attachment = "DROP TABLE recrutements"
        val query_messages = "DROP TABLE messages"
        val query_discussions = "DROP TABLE discussion"
        db.execSQL(query_recrutement)
        db.execSQL(query_recrutement_attachment)
        db.execSQL(query_discussions)
        db.execSQL(query_messages)
        onCreate(db)
    }

    fun insertRecrutement(
            id_recr: String?,
            nom_ent: String?,
            domaine: String?,
            description: String?,
            descr: String?,
            date_pub: String?,
            date_fin: String?,
            heure_fin: String?,
            nom_r: String?,
            vue: String?,
            lien: String?,
            share: String?
    ) {
        // String query = "INSERT INTO recrutements (id_recr, nom_ent, domaine, description, descr, date_pub, date_fin, heure_fin, nom_r, vue, lien, share) VALUES "
        //+ "(\"" + id_recr + "\", \"" + nom_ent + "\", \"" + domaine + "\",  \"" + description + "\", '" + descr + "', '" + date_pub + "', '" + date_fin + "', '" + heure_fin + "', \"" + nom_r + "\", '" + vue + "', '" + lien + "', '" + share + "' )";
        //this.getWritableDatabase().execSQL(query);
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("id_recr", id_recr)
        values.put("nom_ent", nom_ent)
        values.put("domaine", domaine)
        values.put("description", description)
        values.put("descr", descr)
        values.put("date_pub", date_pub)
        values.put("date_fin", date_fin)
        values.put("heure_fin", heure_fin)
        values.put("nom_r", nom_r)
        values.put("vue", vue)
        values.put("lien", lien)
        values.put("share", share)
        db.insert("recrutements", null, values)
    }

    fun insertRecruitAttachment(
            nom: String,
            thumbnail: String,
            id_rec_fk: String
    ) {
        val query = ("INSERT INTO affiche_rec (nom, thumbnail, id_rec_fk) VALUES "
                + "(\"" + nom + "\", \"" + thumbnail + "\", '" + id_rec_fk + "' )")
        this.writableDatabase.execSQL(query)
    }

    fun insertMessages(
            message_id: String?,
            message: String?,
            created_at: String?,
            etat: String?,
            discussion_id: String?,
            type: String?,
            sender: String?
    ) {
        /*String query = "INSERT INTO messages (message_id, message, created_at, etat, discussion_id, type, sender) VALUES "
                + "(\"" + message_id + "\", \"" + message + "\", '" + created_at + "', '" + etat + "', '" + discussion_id + "', '" + type + "', '" + sender + "' )";
        this.getWritableDatabase().execSQL(query);*/
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("message_id", message_id)
        values.put("message", message)
        values.put("created_at", created_at)
        values.put("etat", etat)
        values.put("discussion_id", discussion_id)
        values.put("type", type)
        values.put("sender", sender)
        db.insert("messages", null, values)
    }

    fun insertMessage(
            discussion_id: String,
            user: String,
            id_ann: String,
            annonce: Int,
            titre: String,
            affiche: String? = null
    ) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("discussion_id", discussion_id)
        values.put("user", user)
        values.put("id_ann", id_ann)
        values.put("annonce", annonce)
        values.put("titre", titre)
        values.put("affiche", affiche)
        db.insert("message", null, values)
    }

    val recruits: List<Recrutement>
        get() {
            val recrutements: MutableList<Recrutement> = ArrayList()
            val query = "SELECT * FROM recrutements ORDER BY id_recr DESC"
            val cursor = this.readableDatabase.rawQuery(query, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val recrutement = Recrutement()
                recrutement.id_recr = cursor.getString(0)
                recrutement.nom_ent = cursor.getString(1)
                recrutement.domaine = cursor.getString(2)
                recrutement.description = cursor.getString(3)
                recrutement.desc = cursor.getString(4)
                recrutement.date_pub = cursor.getString(5)
                recrutement.date_fin = cursor.getString(6)
                recrutement.heure_fin = cursor.getString(7)
                recrutement.nom_r = cursor.getString(8)
                recrutement.vue = cursor.getString(9)
                recrutement.lien = cursor.getString(10)
                recrutement.share = cursor.getString(11)
                recrutement.affiches(getAttachments(cursor.getString(0)))
                recrutements.add(recrutement)
                cursor.moveToNext()
            }
            cursor.close()
            return recrutements
        }

    fun getAttachments(id_rec_fk: String): List<Recrutement.Affiche> {
        val affiches: MutableList<Recrutement.Affiche> = ArrayList()
        val query = "SELECT * FROM affiche_rec WHERE id_rec_fk = ?"
        val cursor = this.readableDatabase.rawQuery(query, arrayOf(id_rec_fk))
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val affiche = Recrutement.Affiche()
            affiche.id_recr = cursor.getString(3)
            affiche.thumbnail = cursor.getString(2)
            affiche.nom = cursor.getString(1)
            affiches.add(affiche)
            cursor.moveToNext()
        }
        cursor.close()
        return affiches
    }

    fun getMessages(discussion_id: String): List<Message.Messages> {
        val messages: MutableList<Message.Messages> = ArrayList()
        val query = "SELECT * FROM messages WHERE discussion_id = ?"
        val cursor = this.readableDatabase.rawQuery(query, arrayOf(discussion_id))
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val message = Message.Messages()
            message.message_id = cursor.getString(0)
            message.message = cursor.getString(1)
            message.created_at = cursor.getString(2)
            message.etat = cursor.getString(3)
            message.discussion_id = cursor.getString(4)
            message.isread = cursor.getString(5)
            message.type = cursor.getString(6)
            message.is_deleted = cursor.getString(7)
            message.sender = cursor.getString(8)
            if (message.type == "image") {
                message.image = Message.Messages.Image(message.message)
            }
            messages.add(message)
            cursor.moveToNext()
        }
        cursor.close()
        return messages
    }

    val pendingMessages: List<Message.Messages>
        get() {
            val messages: MutableList<Message.Messages> = ArrayList()
            val query = "SELECT * FROM messages WHERE etat = 1"
            val cursor = this.readableDatabase.rawQuery(query, arrayOf("1"))
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val message = Message.Messages()
                message.message_id = cursor.getString(0)
                message.message = cursor.getString(1)
                message.created_at = cursor.getString(2)
                message.etat = cursor.getString(3)
                message.discussion_id = cursor.getString(4)
                message.isread = cursor.getString(5)
                message.type = cursor.getString(6)
                message.is_deleted = cursor.getString(7)
                message.sender = cursor.getString(8)
                messages.add(message)
                cursor.moveToNext()
            }
            cursor.close()
            return messages
        }

    fun setSentMessages(message_id: String) {
        val messages: List<Message.Messages> = ArrayList()
        val query = "UPDATE messages SET etat = '2' WHERE message_id = '$message_id' "
        this.writableDatabase.execSQL(query)
    }

    fun getMessage(discussion_id: String): Message {
        val query = "SELECT * FROM message WHERE discussion_id = ?"
        val cursor = this.readableDatabase.rawQuery(query, arrayOf(discussion_id))
        cursor.moveToFirst()
        val message = Message()
        while (!cursor.isAfterLast) {
            val user = cursor.getString(1)
            val user1 = User()
            try {
                val `object` = JSONObject(user)
                user1.username = `object`.getString("username")
                user1.photo = `object`.getString("photo")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            message.setUser(user1)
            message.id_ann = cursor.getString(2)
            var isAnnonce = false
            if (cursor.getInt(3) == 1) isAnnonce = true
            message.isAnnonce = isAnnonce
            message.titre = cursor.getString(4)
            message.affiche = cursor.getString(5)
            cursor.close()
            cursor.moveToNext()
        }
        return message
    }

    fun truncateRecruits() {
        val query = "DELETE FROM recrutements"
        val query_attachment = "DELETE FROM affiche_rec"
        this.writableDatabase.execSQL(query)
        this.writableDatabase.execSQL(query_attachment)
    }

    fun truncateMessages() {
        val query = "DELETE FROM messages"
        this.writableDatabase.execSQL(query)
    }

    fun truncateMessage() {
        val query = "DELETE FROM message"
        this.writableDatabase.execSQL(query)
    }

    companion object {
        private const val DATABASE_NAME = "fbn.db"
        private const val DATABASE_VERSION = 1
    }
}