package ma.emsi.dhissiayman.tp0jaka;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * ChatBean — Backing Bean JSF (CDI)
 * Auteur : Ayman Dhissi
 *
 * Ce bean gère toute la logique côté serveur du mini-chat JSF.
 * Il interagit directement avec la page index.xhtml.
 *
 * Fonctions principales :
 *  - Gestion du rôle choisi (assistant, traducteur, chiffreur, rot13…)
 *  - Traitement des messages selon le rôle
 *  - Gestion de l’historique (question, réponse, clé)
 *  - Gestion d’erreurs globales et spécifiques
 */
@Named("chat")              // Nom d’accès dans la page JSF via #{chat}
@ViewScoped                 // Le bean reste vivant tant que la vue est ouverte
public class ChatBean implements Serializable {

    private static final long serialVersionUID = 1L; // Requis pour ViewScoped

    // ======================
    // ==== ATTRIBUTS =======
    // ======================
    private String role;           // Rôle choisi par l’utilisateur
    private boolean roleChoisi;    // Indique si le rôle a été verrouillé
    private String question;       // Texte saisi par l’utilisateur
    private String reponse;        // Réponse calculée côté serveur
    private String clefBase64;     // Clé utilisée pour le chiffrement XOR

    // Historique des échanges : liste de tableaux [question, réponse, clé]
    private final List<String[]> historiqueList = new ArrayList<>();


    // ======================
    // ==== MÉTHODES ========
    // ======================

    /**
     * Verrouille le rôle choisi une fois sélectionné
     */
    public void verrouillerRole() {
        if (role != null && !role.isBlank()) roleChoisi = true;
    }


    /**
     * Méthode principale — appelée lors du clic sur “Envoyer”
     * Gère :
     *  - La validation des champs
     *  - Le traitement selon le rôle
     *  - L’ajout à l’historique
     */
    public String envoyer() {

        // ----- Validation du rôle -----
        if (role == null || role.isBlank()) {
            addError("Veuillez choisir le rôle de l’API.");   // message global
            addError("form:role", "Le rôle est obligatoire"); // message spécifique
            return null;
        }

        // ----- Validation de la question -----
        if (question == null || question.isBlank()) {
            addError("La question est obligatoire.");              // global
            addError("form:question", "La question est obligatoire"); // spécifique
            return null;
        }

        String resultat;
        String clef = null;

        // ----- Traitement selon le rôle choisi -----
        switch (role.toLowerCase()) {

            // === 1️⃣ Rôle ROT13 (chiffrement simple) ===
            case "rot13": {
                resultat = "ROT13: " + rot13(question);
                break;
            }

            // === 2️⃣ Rôle CHIFFREUR (XOR + Base64) ===
            case "chiffreur": {

                // Conversion du texte en bytes
                byte[] plain = question.getBytes(StandardCharsets.UTF_8);

                // Génération d’une clé aléatoire de même taille
                byte[] key = new byte[plain.length];
                new SecureRandom().nextBytes(key);

                // Application du chiffrement XOR
                byte[] cipher = new byte[plain.length];
                for (int i = 0; i < plain.length; i++) {
                    cipher[i] = (byte) (plain[i] ^ key[i]);
                }

                // Encodage Base64 du texte chiffré et de la clé
                String cipherB64 = Base64.getEncoder().encodeToString(cipher);
                String keyB64 = Base64.getEncoder().encodeToString(key);

                // Construction du résultat
                resultat = "Rôle: chiffreur\nTexte chiffré (Base64):\n" + cipherB64;
                clef = keyB64;
                break;
            }

            // === 3️⃣ Traducteur (fr -> en) ===
            case "traducteur français-anglais": {
                resultat = "Traduction (EN): " + question; // simple démo
                break;
            }

            // === 4️⃣ Guide touristique ===
            case "guide touristique": {
                resultat = "Guide: " + question.toUpperCase(); // simple démo
                break;
            }

            // === 5️⃣ Assistant par défaut ===
            default: {
                resultat = "Assistant: " + inverserCasse(question); // inverse la casse du texte
            }
        }

        // Stockage du résultat et de la clé dans le bean
        this.reponse = resultat;
        this.clefBase64 = clef;

        // Ajout de l’échange à l’historique
        historiqueList.add(new String[]{question, resultat, clef});

        // Réinitialisation du champ question
        this.question = "";

        return null; // Rester sur la même page
    }


    /**
     * Supprime le dernier message de l’historique
     */
    public String effacerDerniere() {
        if (!historiqueList.isEmpty()) {
            historiqueList.remove(historiqueList.size() - 1);

            // Mise à jour de la réponse et clé affichée
            if (historiqueList.isEmpty()) {
                reponse = "";
                clefBase64 = null;
            } else {
                String[] last = historiqueList.get(historiqueList.size() - 1);
                reponse = last[1];
                clefBase64 = last[2];
            }
        }
        return null;
    }


    /**
     * Redirige vers une nouvelle session (nouveau chat)
     */
    public String nouveauChat() {
        return "index.xhtml?faces-redirect=true";
    }


    /**
     * Retourne l’historique sous forme de texte formaté
     */
    public String getHistorique() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < historiqueList.size(); i++) {
            String[] entry = historiqueList.get(i);
            sb.append("Q").append(i + 1).append(": ").append(entry[0]).append("\n");
            sb.append("R").append(i + 1).append(": ").append(entry[1]).append("\n");
            if (entry[2] != null) sb.append("Clé(Base64): ").append(entry[2]).append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }


    // ======================
    // ==== UTILITAIRES =====
    // ======================

    /**
     * Inverse la casse d’une chaîne (a→A, B→b)
     */
    private static String inverserCasse(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) out.append(Character.toLowerCase(c));
            else if (Character.isLowerCase(c)) out.append(Character.toUpperCase(c));
            else out.append(c);
        }
        return out.toString();
    }

    /**
     * Implémentation du chiffrement ROT13
     * (Décale chaque lettre de 13 positions dans l’alphabet)
     */
    private static String rot13(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                sb.append((char) ((c - 'a' + 13) % 26 + 'a'));
            } else if (c >= 'A' && c <= 'Z') {
                sb.append((char) ((c - 'A' + 13) % 26 + 'A'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Ajoute un message d’erreur global
     */
    private void addError(String msg) {
        addError(null, msg);
    }

    /**
     * Ajoute un message d’erreur spécifique (lié à un champ)
     */
    private void addError(String clientId, String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    // ======================
    // ==== GETTERS/SETTERS =
    // ======================
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isRoleChoisi() { return roleChoisi; }
    public void setRoleChoisi(boolean roleChoisi) { this.roleChoisi = roleChoisi; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }
    public String getClefBase64() { return clefBase64; }
    public void setClefBase64(String clefBase64) { this.clefBase64 = clefBase64; }

    // Utilisé pour afficher ou non la zone “clé” dans la vue
    public boolean isChiffreur() { return "chiffreur".equalsIgnoreCase(role); }
    public boolean getChiffreur() { return isChiffreur(); }
}
