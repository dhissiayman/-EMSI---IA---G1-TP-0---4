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

@Named("chat")
@ViewScoped
public class ChatBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String role;
    private boolean roleChoisi;
    private String question;
    private String reponse;
    private String clefBase64;

    // Historique : [question, réponse, clé|null]
    private final List<String[]> historiqueList = new ArrayList<>();

    // Verrouiller le rôle après sélection
    public void verrouillerRole() {
        if (role != null && !role.isBlank()) roleChoisi = true;
    }

    // Envoyer la question
    public String envoyer() {
        // ----- Validation rôle -----
        if (role == null || role.isBlank()) {
            addError("Veuillez choisir le rôle de l’API.");   // global
            addError("form:role", "Le rôle est obligatoire"); // spécifique
            return null;
        }

        // ----- Validation question -----
        if (question == null || question.isBlank()) {
            addError("La question est obligatoire.");             // global
            addError("form:question", "La question est obligatoire"); // spécifique
            return null;
        }

        String resultat;
        String clef = null;

        switch (role.toLowerCase()) {
            case "chiffreur": {
                // Chiffrement XOR + clé aléatoire
                byte[] plain = question.getBytes(StandardCharsets.UTF_8);
                byte[] key = new byte[plain.length];
                new SecureRandom().nextBytes(key);

                byte[] cipher = new byte[plain.length];
                for (int i = 0; i < plain.length; i++) {
                    cipher[i] = (byte) (plain[i] ^ key[i]);
                }

                String cipherB64 = Base64.getEncoder().encodeToString(cipher);
                String keyB64 = Base64.getEncoder().encodeToString(key);

                resultat = "Rôle: chiffreur\nTexte chiffré (Base64):\n" + cipherB64;
                clef = keyB64;
                break;
            }
            case "traducteur français-anglais": {
                resultat = "Traduction (EN): " + question; // démo simple
                break;
            }
            case "guide touristique": {
                resultat = "Guide: " + question.toUpperCase(); // démo simple
                break;
            }
            default: {
                resultat = "Assistant: " + inverserCasse(question); // inversion de casse
            }
        }

        // Affectations pour l'UI
        this.reponse = resultat;
        this.clefBase64 = clef;

        // Historique (Q, R, Clé)
        historiqueList.add(new String[]{question, resultat, clef});

        // Prépare la prochaine saisie
        this.question = "";

        return null; // rester sur la même vue
    }

    // Effacer la dernière entrée de l'historique
    public String effacerDerniere() {
        if (!historiqueList.isEmpty()) {
            historiqueList.remove(historiqueList.size() - 1);
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

    // Redémarrer la vue
    public String nouveauChat() {
        return "index.xhtml?faces-redirect=true";
    }

    // Rendu de l'historique
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

    // Inversion de casse
    private static String inverserCasse(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) out.append(Character.toLowerCase(c));
            else if (Character.isLowerCase(c)) out.append(Character.toUpperCase(c));
            else out.append(c);
        }
        return out.toString();
    }

    // Helpers messages d'erreur
    private void addError(String msg) { // global
        addError(null, msg);
    }
    private void addError(String clientId, String msg) { // spécifique si clientId != null
        FacesContext.getCurrentInstance()
                .addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    // Getters / Setters
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

    // Pour l'affichage conditionnel de la zone "Clé (Base64)"
    public boolean isChiffreur() { return "chiffreur".equalsIgnoreCase(role); }
    public boolean getChiffreur() { return isChiffreur(); }
}
