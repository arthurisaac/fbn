package bf.fasobizness.bafatech.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import bf.fasobizness.bafatech.R

class ActivityAdvices : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advices)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "05 astuces"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }

        val conseilAnnonce = findViewById<TextView>(R.id.conseilAnnonce)
        val conseil = "<p><strong>05 astuces pour cr&eacute;er une annonce qui fait vendre&nbsp;!</strong></p>\n" +
                "<p><span style=\"font-weight: 400;\">Si vous souhaitez cr&eacute;er une annonce &agrave; succ&egrave;s, nous vous recommandons de suivre les consignes ci-dessous :</span></p>\n" +
                "<p>&nbsp;</p>\n" +
                "<p><strong><strong>Titre accrocheur</strong></strong></p>\n" +
                "<p><span style=\"font-weight: 400;\">Utilisez un titre clair et accrocheur, incluant le nom de l&rsquo;article que vous vendez.</span></p>\n" +
                "<p>&nbsp;<strong><strong>Description d&eacute;taill&eacute;e</strong></strong></p>\n" +
                "<p><span style=\"font-weight: 400;\">La description de votre produit doit &ecirc;tre pr&eacute;cise et compl&egrave;te. Plus il y a de d&eacute;tails, plus vous avez de chances d&rsquo;apparaitre dans les r&eacute;sultats de recherche.</span></p>\n" +
                "<p>&nbsp;<strong><strong>Des images de qualit&eacute;</strong></strong></p>\n" +
                "<p><span style=\"font-weight: 400;\">En ajoutant de belles photos qui mettent en valeur le produit, votre annonce a beaucoup plus de chance d&rsquo;attirer et convaincre les acheteurs potentiels.</span></p>\n" +
                "<p>&nbsp;<strong><strong>Choisir la bonne cat&eacute;gorie</strong></strong></p>\n" +
                "<p><span style=\"font-weight: 400;\">Il est important de veiller &agrave; publier votre annonce dans la bonne cat&eacute;gorie. Les acheteurs font tr&egrave;s souvent leurs recherches dans la cat&eacute;gorie qui les int&eacute;resse.&nbsp;</span></p>\n" +
                "<p>&nbsp;<strong><strong>Fixer le juste prix&nbsp;</strong></strong></p>\n" +
                "<p><span style=\"font-weight: 400;\">D&eacute;finissez un prix appropri&eacute; pour votre article afin que l'annonce retienne l&rsquo;attention de potentiels acheteurs.&nbsp;</span></p>\n" +
                "<p><br /><br /></p>"
        conseilAnnonce.text = HtmlCompat.fromHtml(conseil, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}