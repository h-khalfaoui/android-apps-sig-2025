package com.example.medcare.model;

public class ChatBot {

    public String getResponse(String question) {

        question = question.trim().toLowerCase();

        // Question concernant l'ajout d'un établissement
        if (question.contains("ajouter un établissement")) {
            return "Pour ajouter un établissement, vous devez remplir un formulaire avec les informations nécessaires comme le nom, l'adresse, le téléphone, etc.";
        }
        // Question concernant les établissements disponibles
        else if (question.contains("établissements disponibles") || question.contains("liste des établissements")) {
            return "Vous pouvez consulter les détails de chaque établissement depuis l'interface 'Établissements'.";
        }
        // Question concernant la file d'attente
        else if (question.contains("file d'attente") || question.contains("voir la file d'attente")) {
            return "Vous pouvez consulter la file d'attente dans la section 'File d'attente' de l'application. La file est mise à jour en temps réel.";
        }
        // Question concernant la modification du profil
        else if (question.contains("modifier mon profil") || question.contains("changer mes informations")) {
            return "Pour modifier vos informations, allez dans la section 'Profil' et modifiez les champs nécessaires, comme votre nom, email ou spécialité.";
        }
        // Question concernant les informations sur les établissements
        else if (question.contains("détails de l'établissement") || question.contains("informations établissement")) {
            return "Vous pouvez consulter les détails d'un établissement en cliquant sur son nom dans la liste des établissements. Vous y trouverez l'adresse, les contacts, et plus.";
        }
        // Question concernant les disponibilités
        else if (question.contains("disponibilités") || question.contains("horaires de travail")) {
            return "Les disponibilités des établissements et des professionnels de santé sont disponibles dans la section 'Disponibilités'. Vous pouvez consulter et mettre à jour vos horaires.";
        }
        // Question concernant la suppression d'un établissement
        else if (question.contains("supprimer un établissement")) {
            return "Pour supprimer un établissement, vous devez contacter un administrateur ou utiliser l'option 'Supprimer' dans les paramètres de l'établissement.";
        }
        // Question concernant les rôles des utilisateurs
        else if (question.contains("rôles des utilisateurs")) {
            return "Les utilisateurs de l'application peuvent avoir différents rôles : administrateur, professionnel de santé, ou utilisateur standard.";
        }
        // Question concernant la gestion des utilisateurs
        else if (question.contains("ajouter un utilisateur") || question.contains("gérer les utilisateurs")) {
            return "Pour ajouter ou gérer les utilisateurs, vous devez être un administrateur. Accédez à la section 'Utilisateurs' dans le menu d'administration.";
        }
        // Question concernant la sécurité
        else if (question.contains("sécurité") || question.contains("protection des données")) {
            return "Nous prenons la sécurité de vos données très au sérieux. Toutes les informations sont cryptées et stockées de manière sécurisée.";
        }
        // Question concernant l'assistance
        else if (question.contains("aide") || question.contains("support")) {
            return "Si vous avez besoin d'aide, vous pouvez consulter notre FAQ ou contacter notre support via la section 'Assistance' dans l'application.";
        }
        // Question concernant l'application en général
        else if (question.contains("qu'est-ce que cette application") || question.contains("fonction de l'application")) {
            return "Cette application permet de gérer les établissements de santé, les disponibilités des professionnels, et la file d'attente des patients, le tout dans une interface simple et intuitive.";
        }
        // Ajout de nouvelles questions
        else if (question.contains("horaires d'ouverture") || question.contains("quand ouvre l'établissement")) {
            return "Les horaires d'ouverture des établissements sont disponibles dans la section 'Disponibilités'.";
        }
        else if (question.contains("comment s'inscrire") || question.contains("comment créer un compte")) {
            return "Vous pouvez vous inscrire en remplissant un formulaire dans la section 'Inscription'. Vous devrez fournir votre nom, email, et votre numéro de licence.";
        }
        else if (question.contains("comment modifier ma spécialité") || question.contains("changer ma spécialité")) {
            return "Pour changer votre spécialité, allez dans la section 'Profil' et modifiez le champ 'Spécialité'.";
        }
        else if (question.contains("comment ajouter des horaires") || question.contains("ajouter des disponibilités")) {
            return "Pour ajouter des horaires, allez dans la section 'Disponibilités' et sélectionnez l'établissement et l'heure souhaitée.";
        }
        else if (question.contains("comment consulter les détails d’un établissement") || question.contains("informations sur un établissement")) {
            return "Pour consulter les détails d’un établissement, cliquez sur son nom dans la liste des établissements et vous y trouverez toutes les informations.";
        }
        else if (question.contains("comment ajouter ou gérer un utilisateur")) {
            return "Pour ajouter ou gérer un utilisateur, vous devez être administrateur et avoir accès à la section 'Gestion des utilisateurs'.";
        }
        // Question non reconnue
        else {
            return "Désolé, je n'ai pas compris votre question. Pouvez-vous reformuler ?";
        }
    }

}