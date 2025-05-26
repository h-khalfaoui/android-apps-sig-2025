package com.example.geotourisme;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.example.geotourisme.data.db.AppDatabase;
import com.example.geotourisme.data.db.CommentaireDao;
import com.example.geotourisme.data.db.ReviewDao;
import com.example.geotourisme.data.db.SiteDao;
import com.example.geotourisme.data.db.UserDao;
import com.example.geotourisme.model.Commentaire;
import com.example.geotourisme.model.Review;
import com.example.geotourisme.model.Site;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SiteDetailActivity extends AppCompatActivity {

    ImageView detailImage;
    TextView detailTitle, detailDesc, detailCoords, detailLoc;
    TextView detailNature, detailEmplacement;

    private UserDao userDao;
    static final int PICK_IMAGE_REQUEST = 1;
    private SiteDao siteDao;
    private int currentSiteId;
    private Uri selectedImageUri;
    private ImageView previewImageView; // Pour afficher un aperçu de l'image sélectionnée


    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_detail);

        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);
        detailDesc = findViewById(R.id.detailDesc);
        detailCoords = findViewById(R.id.detailCoords);
        //detailLoc = findViewById(R.id.detailLoc);
        detailNature = findViewById(R.id.natureReliefText);
        detailEmplacement = findViewById(R.id.emplacementText);
        TextView visitsTextView = findViewById(R.id.visitesTextView);

        ImageView shareIcon = findViewById(R.id.icon_share);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        EditText usernameInput = findViewById(R.id.usernameInput);
        Button submitReviewBtn = findViewById(R.id.submitReviewBtn);

        AppDatabase db = AppDatabase.getInstance(this);
        ReviewDao reviewDao = db.reviewDao();
        userDao = db.userDao();

        currentSiteId = getIntent().getIntExtra("id", -1);
        if (currentSiteId == -1) {
            Toast.makeText(this, "Invalid site!", Toast.LENGTH_SHORT).show();
            finish();
        }
        siteDao= db.siteDao();

        Intent i = getIntent();
        String nom = i.getStringExtra("nom"); // ⬅️ nom site
        String desc = i.getStringExtra("description");
        String loc = i.getStringExtra("localisation");
        double lat = i.getDoubleExtra("latitude", 0);
        double lon = i.getDoubleExtra("longitude", 0);
        String natureRelief = i.getStringExtra("nature_relief");



        new Thread(() -> {
            Log.d("SiteDetail", "Incrementing visits for site ID: " + currentSiteId);
            siteDao.incrementVisites(currentSiteId);
            int totalVisits = siteDao.getVisitsForSite(currentSiteId);
            Log.d("SiteDetail", "New visit count: " + totalVisits);
            runOnUiThread(() -> {
                visitsTextView.setText("👥 " + totalVisits);
            });
        }).start();


        String siteName = nom;

        // remplir les champs
        detailTitle.setText(nom);
        detailDesc.setText(desc);
        //detailLoc.setText("📍 " + loc);
        detailCoords.setText(" " + lat + ", " + lon);
        detailNature.setText(" " + natureRelief);
        detailEmplacement.setText(" " + loc);




        byte[] byteArray = i.getByteArrayExtra("imageBitmap");
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            detailImage.setImageBitmap(bitmap);
        } else {
            detailImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // Chargement du review s’il existe
        new Thread(() -> {
            currentUserId = getCurrentUserId();
            Review existingReview = reviewDao.getUserReviewForSite(currentUserId, siteName);
            runOnUiThread(() -> {
                if (existingReview != null) {
                    ratingBar.setRating(existingReview.rating);
                    ratingBar.setIsIndicator(true);

                    new Thread(() -> {
                        String username = userDao.getUsernameById(currentUserId);
                        runOnUiThread(() -> {
                            usernameInput.setText("⭐ Par: " + username);
                        });
                    }).start();

                    usernameInput.setEnabled(false);
                    submitReviewBtn.setVisibility(View.GONE);
                } else {
                    // pas encore noté
                    submitReviewBtn.setOnClickListener(v -> {
                        float rating = ratingBar.getRating();
                        String username = usernameInput.getText().toString();

                        Review newReview = new Review();
                        newReview.userId = currentUserId;
                        newReview.siteName = siteName;
                        newReview.rating = rating;

                        new Thread(() -> {
                            reviewDao.insert(newReview);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Merci pour votre note !", Toast.LENGTH_SHORT).show();
                                recreate();
                            });
                        }).start();
                    });
                }
            });
        }).start();

//        // incrémenter les visites
//        incrementVisites(currentUserId);

        // visites count
//        int visitesCount = userDao.getVisitesCount(currentUserId);
//        TextView visitsTextView = findViewById(R.id.visitesTextView);
//        visitsTextView.setText("\uD83D\uDC65 " + visitesCount);
        Button chooseImageBtn = findViewById(R.id.btnChooseImage);
        previewImageView = findViewById(R.id.previewImageView); // Tu dois ajouter cette ImageView dans ton layout aussi

        chooseImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Sélectionner une image"), PICK_IMAGE_REQUEST);
        });
        CommentaireDao commentaireDao = db.commentaireDao();
        EditText commentInput = findViewById(R.id.commentInput);
        Button submitCommentBtn = findViewById(R.id.submitCommentBtn);
        submitCommentBtn.setVisibility(View.GONE);
        commentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    submitCommentBtn.setVisibility(View.GONE);
                } else {
                    submitCommentBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        LinearLayout commentsContainer = findViewById(R.id.commentsContainer);

        submitCommentBtn.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();

            if (!commentText.isEmpty()) {
                Commentaire commentaire = new Commentaire();
                commentaire.contenu = commentText;
                commentaire.id_user = getCurrentUserId();
                commentaire.id_site = currentSiteId;
                commentaire.date_commentaire = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        byte[] imageBytes = new byte[inputStream.available()];
                        inputStream.read(imageBytes);
                        commentaire.image = imageBytes; // Ajoute ce champ dans ton modèle
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                new Thread(() -> {
                    commentaireDao.insert(commentaire);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Commentaire ajouté avec image !", Toast.LENGTH_SHORT).show();
                        commentInput.setText("");
                        selectedImageUri = null;
                        previewImageView.setImageDrawable(null);
                        previewImageView.setVisibility(View.GONE);
                        loadCommentaires(commentaireDao, commentsContainer);
                    });
                }).start();
            }
        });

// Charger tous les commentaires existants pour ce site
        loadCommentaires(commentaireDao, commentsContainer);

        // share icon
        shareIcon.setOnClickListener(v -> {
            String shareText = "📍 " + detailTitle.getText().toString() + "\n"
                    + detailDesc.getText().toString() + "\n"
                    //+ "🗺️ Lieu : " + detailLoc.getText().toString() + "\n"
                    + "🔗 En savoir plus : https://geotourisme.ma/site?nom=" + detailTitle.getText().toString().replace(" ", "%20") + "\n"
                    + "📲 Télécharge l'app : https://play.google.com/store/apps/details?id=com.example.geotourisme";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Découvre ce lieu !");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Partager via"));
        });

        Button suggestButton = findViewById(R.id.btn_suggest_change);
        suggestButton.setOnClickListener(v -> {
            Intent intent = new Intent(SiteDetailActivity.this, SuggestChangeActivity.class);
            startActivity(intent);
        });
    }

//    private void incrementVisites(int userId) {
//        new Thread(() -> userDao.incrementVisites(userId)).start();
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            previewImageView.setVisibility(View.VISIBLE);
            previewImageView.setImageURI(selectedImageUri);
        }
    }

    private int getCurrentUserId() {
        return 1; // Remplace par ton vrai système de session
    }
    private void loadCommentaires(CommentaireDao commentaireDao, LinearLayout container) {
        new Thread(() -> {
            List<Commentaire> commentaires = commentaireDao.getCommentairesForSite(currentSiteId);
            List<String> usernames = new ArrayList<>();

            for (Commentaire c : commentaires) {
                usernames.add(userDao.getUsernameById(c.id_user));
            }

            runOnUiThread(() -> {
                container.removeAllViews();
                for (int i = 0; i < commentaires.size(); i++) {
                    Commentaire c = commentaires.get(i);
                    String username = usernames.get(i);

                    TextView commentView = new TextView(this);
                    commentView.setText("🗨️ " + username + " : " + c.contenu + " \n📅 " + c.date_commentaire);
                    commentView.setPadding(8, 8, 8, 16);
                    container.addView(commentView);

                    // Ajouter l'image associée au commentaire s'il y en a une
                    if (c.image != null) {
                        ImageView img = new ImageView(this);
                        Bitmap bmp = BitmapFactory.decodeByteArray(c.image, 0, c.image.length);
                        img.setImageBitmap(bmp);
                        img.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
                        container.addView(img);
                    }
                }
            });
        }).start();
    }

}
