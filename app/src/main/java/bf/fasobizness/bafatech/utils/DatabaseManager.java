package bf.fasobizness.bafatech.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import bf.fasobizness.bafatech.models.Recruit;

public class DatabaseManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fbn.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query_recrutement = "CREATE TABLE recrutements ("
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
                + ")";
        db.execSQL(query_recrutement);

        String query_recruit_attachment = "CREATE TABLE affiche_rec ("
                + " id_recr integer PRIMARY KEY autoincrement,"
                + " nom varchar(255),"
                + " thumbnail varchar(255),"
                + " id_rec_fk integer"
                + ")";
        db.execSQL(query_recruit_attachment);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query_recrutement = "DROP TABLE recrutements";
        String query_recrutement_attachment = "DROP TABLE recrutements";
        db.execSQL(query_recrutement);
        db.execSQL(query_recrutement_attachment);

        this.onCreate(db);
    }

    public void insertRecrutement(
            String id_recr,
            String nom_ent,
            String domaine,
            String description,
            String descr,
            String date_pub,
            String date_fin,
            String heure_fin,
            String nom_r,
            String vue,
            String lien,
            String share
    ) {
        String query = "INSERT INTO recrutements (id_recr, nom_ent, domaine, description, descr, date_pub, date_fin, heure_fin, nom_r, vue, lien, share) VALUES "
                + "(\"" + id_recr + "\", \"" + nom_ent + "\", \"" + domaine + "\",  \"" + description + "\", '" + descr + "', '" + date_pub + "', '" + date_fin + "', '" + heure_fin + "', \"" + nom_r + "\", '" + vue + "', '" + lien + "', '" + share + "' )";
        this.getWritableDatabase().execSQL(query);

    }

    public void insertRecruitAttachment(
            String nom,
            String thumbnail,
            String id_rec_fk
    ) {
        String query = "INSERT INTO affiche_rec (nom, thumbnail, id_rec_fk) VALUES "
                + "(\"" + nom + "\", \"" + thumbnail + "\", '" + id_rec_fk + "' )";
        this.getWritableDatabase().execSQL(query);
    }

    public List<Recruit.Recrutement> getRecruits() {
        List<Recruit.Recrutement> recrutements = new ArrayList<>();
        String query = "SELECT * FROM recrutements ORDER BY id_recr DESC";
        Cursor cursor = this.getReadableDatabase().rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            Recruit.Recrutement recrutement = new Recruit.Recrutement();
            recrutement.setId_recr(cursor.getString(0));
            recrutement.setNom_ent(cursor.getString(1));
            recrutement.setDomaine(cursor.getString(2));
            recrutement.setDescription(cursor.getString(3));
            recrutement.setDesc(cursor.getString(4));
            recrutement.setDate_pub(cursor.getString(5));
            recrutement.setDate_fin(cursor.getString(6));
            recrutement.setHeure_fin(cursor.getString(7));
            recrutement.setNom_r(cursor.getString(8));
            recrutement.setVue(cursor.getString(9));
            recrutement.setLien(cursor.getString(10));
            recrutement.setShare(cursor.getString(11));
            recrutement.affiches(getAttachments(cursor.getString(0)));

            recrutements.add(recrutement);
            cursor.moveToNext();
        }
        cursor.close();
        return recrutements;
    }

    public List<Recruit.Recrutement.Affiche> getAttachments(String id_rec_fk) {
        List<Recruit.Recrutement.Affiche> affiches = new ArrayList<>();
        String query = "SELECT * FROM affiche_rec WHERE id_rec_fk = ?";
        Cursor cursor = this.getReadableDatabase().rawQuery(query, new String[]{id_rec_fk});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Recruit.Recrutement.Affiche affiche = new Recruit.Recrutement.Affiche();
            affiche.setId_recr(cursor.getString(3));
            affiche.setThumbnail(cursor.getString(2));
            affiche.setNom(cursor.getString(1));
            affiches.add(affiche);
            cursor.moveToNext();
        }
        cursor.close();
        return affiches;
    }

    public void truncateRecruits() {
        String query = "DELETE FROM recrutements";
        String query_attachment = "DELETE FROM affiche_rec";
        this.getWritableDatabase().execSQL(query);
        this.getWritableDatabase().execSQL(query_attachment);
    }
}
