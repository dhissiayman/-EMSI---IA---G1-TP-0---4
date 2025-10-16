# 🎓 EMSI - IA - G1 | TP 0 - Introduction à Jakarta EE

## 📘 Description du projet
Application Web **Jakarta EE (JSF + CDI)** réalisée pour le **TP 0**.  
Objectif : prendre en main **Jakarta Faces (JSF)** et **CDI** via une mini-appli de chat avec rôles et historique.

L’application permet :
- de saisir une **question** et l’envoyer au **serveur**,
- de produire une **réponse** selon un **rôle** choisi,
- d’afficher un **historique** des échanges (et des clés pour le chiffreur),
- d’illustrer **CDI** (scopes, beans) et le rendu **JSF/PrimeFaces**.

---

## 🧠 Fonctionnalités

### Rôles prédéfinis
- `helpful assistant` → réponse générique (démonstration).
- `traducteur français-anglais` → simulation simple côté serveur.
- `guide touristique` → met la réponse en **MAJUSCULES**.

### Rôles personnalisés
- `chiffreur (cryptage)` 🔐
    - Chiffre le texte via **XOR** octet par octet avec une **clé aléatoire** générée côté serveur.
    - Affiche le **texte chiffré** et la **clé** en **Base64**.
    - La clé est **conservée dans l’historique**.
- `ROT13 (chiffrement simple)` 🔄
    - Applique **ROT13** sur les lettres (réversible : ROT13(ROT13(x)) = x).

### Interface
- **JSF/PrimeFaces** : `p:selectOneMenu`, `p:messages`, `p:message`, `p:commandButton`.
- Zones **Question**, **Réponse**, **Historique** (à droite), **Clé** (visible uniquement en mode *chiffreur*).
- Boutons : **Envoyer**, **Effacer la dernière**, **Nouveau chat** (réinitialise la vue/rôle).

---

## ⚠️ Gestion des erreurs (validation)
L’app gère les erreurs **de deux façons complémentaires** :

1) **Message global** (bannière)  
   Affiché via `<p:messages globalOnly="true" autoUpdate="true" ...>` quand une règle générale échoue.
```java
FacesContext.getCurrentInstance().addMessage(
  null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "La question est obligatoire.", null)
);
```

2) **Message spécifique au champ**  
   Affiché sous le composant via `<p:message for="question"/>` ou `<p:message for="role"/>`.
```java
FacesContext.getCurrentInstance().addMessage(
  "form:question", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Veuillez saisir une question.", null)
);
```

> Résultat : feedback clair **sous le champ** concerné + **bannière** globale sans doublons.

---

## ⚙️ Pile technique
- **Jakarta EE 11** (JSF/Faces + CDI)
- **PrimeFaces 15 (classifier `jakarta`)**
- **Payara Server 6.x**
- **Maven**
- **Java 17+** (le projet peut être compilé en 17 ou 21)

---

## 📂 Structure
```
src/
 ├─ main/
 │  ├─ java/
 │  │   └─ ma/emsi/dhissiayman/tp0jaka/
 │  │        └─ ChatBean.java           # Backing bean (CDI + ViewScoped)
 │  ├─ webapp/
 │  │   ├─ index.xhtml                  # Vue JSF/PrimeFaces
 │  │   ├─ resources/
 │  │   │   ├─ css/styles.css
 │  │   │   └─ js/app.js                # copyToClipboard(...)
 │  │   └─ WEB-INF/
 │  │        ├─ web.xml                 # welcome-file index.xhtml
 │  │        └─ beans.xml               # activation CDI (bean-discovery-mode="annotated")
 └─ pom.xml
```

---

## 🚀 Lancer le projet

1. **Cloner**
   ```bash
   git clone https://github.com/dhissiayman/-EMSI---IA---G1-TP-0---4.git
   cd -EMSI---IA---G1-TP-0---4
   ```

2. **Ouvrir dans l’IDE** (IntelliJ IDEA / Eclipse EE)
    - Recharger **Maven** si demandé.

3. **Configurer Payara 6.x**
    - Déploiement : **Exploded WAR**
    - Page d’accueil : `index.xhtml`

4. **Run** (ou Shift+F10) puis accéder à :
   ```
   http://localhost:8080/<context-path>/index.xhtml
   ```

---

## 💬 Exemple

**Entrée**
```
Rôle : ROT13
Question : Bonjour le monde !
```

**Sortie**
```
ROT13: Obawhbe yr zabqr !
```

**Entrée**
```
Rôle : chiffreur
Question : secret
```

**Sortie (exemple)**
```
Rôle: chiffreur
Texte chiffré (Base64):
q9cPJg==
```
**Clé (Base64)**
```
g2yG0A==
```

Historique :
```
Q1: secret
R1: Rôle: chiffreur
    Texte chiffré (Base64):
    q9cPJg==
Clé(Base64): g2yG0A==
```

---

## 🧾 Erreur “question vide” (comportement)
- Clic sur **Envoyer** sans saisir de question :
    - `ChatBean.envoyer()` ajoute :
        - un **message spécifique** → `clientId = "form:question"`
        - un **message global** → `clientId = null`
    - `index.xhtml` affiche :
        - `<p:message for="question"/>` → sous le champ
        - `<p:messages globalOnly="true"/>` → bannière globale

---

## 🏆 Bonus réalisé
- Rôle **chiffreur (XOR + Base64)** avec **clé aléatoire** et **historique des clés**.
- Rôle **ROT13** (chiffrement simple réversible).
- **Validation** avec messages **globaux** et **spécifiques**.

---

## 🧑‍💻 Auteur
**DHISSI Ayman**  
EMSI Casablanca(ROUDANI/MAARIF) — TP 0 (JSF/CDI)  
Professeur : **M. Richard Grin**
