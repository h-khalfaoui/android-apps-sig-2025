package com.example.geotourisme.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.geotourisme.model.Commentaire;

import java.util.List;

@Dao
public interface CommentaireDao {
    @Insert
    void insert(Commentaire commentaire);

    @Query("SELECT * FROM commentaires WHERE id_site = :siteId ORDER BY date_commentaire DESC")
    List<Commentaire> getCommentairesForSite(int siteId);
}
