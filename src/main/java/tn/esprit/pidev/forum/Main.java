package tn.esprit.pidev.forum;

import tn.esprit.pidev.forum.entities.Commentaire;
import tn.esprit.pidev.forum.entities.Post;
import tn.esprit.pidev.forum.services.CommentaireService;
import tn.esprit.pidev.forum.services.PostService;
import tn.esprit.pidev.forum.utils.DatabaseConnection;

import java.util.List;
import java.util.Scanner;

/**
 * Classe principale pour tester toutes les fonctionnalités du forum
 */
public class Main {
    private static PostService postService = new PostService();
    private static CommentaireService commentaireService = new CommentaireService();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("===============1==================");
        System.out.println("  FORUM - GESTION DES POSTS");
        System.out.println("=================================\n");

        // Test de connexion à la base de données
        if (DatabaseConnection.getConnection() != null) {
            System.out.println("✅ Connexion réussie!\n");

            // Menu principal
            boolean running = true;
            while (running) {
                afficherMenu();
                int choix = scanner.nextInt();
                scanner.nextLine(); // Consommer la nouvelle ligne

                switch (choix) {
                    case 1:
                        ajouterPost();
                        break;
                    case 2:
                        afficherTousLesPosts();
                        break;
                    case 3:
                        afficherPostsParCategorie();
                        break;
                    case 4:
                        modifierPost();
                        break;
                    case 5:
                        supprimerPost();
                        break;
                    case 6:
                        likerPost();
                        break;
                    case 7:
                        afficherDetailsPost();
                        break;
                    case 8:
                        ajouterCommentaire();
                        break;
                    case 9:
                        likerCommentaire();
                        break;
                    case 0:
                        running = false;
                        System.out.println("Au revoir!");
                        break;
                    default:
                        System.out.println("Choix invalide!");
                }

                if (running) {
                    System.out.println("\nAppuyez sur Entrée pour continuer...");
                    scanner.nextLine();
                }
            }

        } else {
            System.out.println("❌ Impossible de se connecter à la base de données!");
        }
    }

    private static void afficherMenu() {
        System.out.println("\n=================================");
        System.out.println("         MENU PRINCIPAL");
        System.out.println("=================================");
        System.out.println("1. Ajouter un post");
        System.out.println("2. Afficher tous les posts");
        System.out.println("3. Afficher posts par catégorie");
        System.out.println("4. Modifier un post");
        System.out.println("5. Supprimer un post");
        System.out.println("6. Liker un post");
        System.out.println("7. Afficher détails d'un post");
        System.out.println("8. Ajouter un commentaire");
        System.out.println("9. Liker un commentaire");
        System.out.println("0. Quitter");
        System.out.print("\nVotre choix: ");
    }

    private static void ajouterPost() {
        System.out.println("\n--- AJOUTER UN POST ---");

        System.out.print("ID de l'auteur: ");
        int id_auteur = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Titre: ");
        String titre = scanner.nextLine();

        System.out.print("Contenu: ");
        String contenu = scanner.nextLine();

        System.out.print("Catégorie (Anxiété/Dépression/Bien-être/General): ");
        String categorie = scanner.nextLine();

        Post post = new Post(id_auteur, titre, contenu, categorie);

        if (postService.addPost(post)) {
            System.out.println("✅ Post ajouté avec succès!");
        }
    }

    private static void afficherTousLesPosts() {
        System.out.println("\n--- TOUS LES POSTS ---");
        List<Post> posts = postService.getAllPosts();

        if (posts.isEmpty()) {
            System.out.println("Aucun post trouvé.");
        } else {
            for (Post post : posts) {
                afficherPost(post);
            }
        }
    }

    private static void afficherPostsParCategorie() {
        System.out.print("\nCatégorie: ");
        String categorie = scanner.nextLine();

        List<Post> posts = postService.getPostsByCategory(categorie);

        if (posts.isEmpty()) {
            System.out.println("Aucun post trouvé dans cette catégorie.");
        } else {
            for (Post post : posts) {
                afficherPost(post);
            }
        }
    }

    private static void modifierPost() {
        System.out.print("\nID du post à modifier: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        Post post = postService.getPostById(id);
        if (post != null) {
            System.out.print("Nouveau titre (actuel: " + post.getTitre() + "): ");
            String titre = scanner.nextLine();

            System.out.print("Nouveau contenu: ");
            String contenu = scanner.nextLine();

            System.out.print("Nouvelle catégorie (actuelle: " + post.getCategorie() + "): ");
            String categorie = scanner.nextLine();

            post.setTitre(titre);
            post.setContenu(contenu);
            post.setCategorie(categorie);

            if (postService.updatePost(post)) {
                System.out.println("✅ Post modifié avec succès!");
            }
        }
    }

    private static void supprimerPost() {
        System.out.print("\nID du post à supprimer: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        postService.deletePost(id);
    }

    private static void likerPost() {
        System.out.print("\nID du post à liker: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        postService.likePost(id);
    }

    private static void afficherDetailsPost() {
        System.out.print("\nID du post: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        Post post = postService.getPostById(id);
        if (post != null) {
            afficherPost(post);

            // Afficher les commentaires
            System.out.println("\n--- COMMENTAIRES ---");
            List<Commentaire> commentaires = commentaireService.getCommentairesByPost(id);

            if (commentaires.isEmpty()) {
                System.out.println("Aucun commentaire.");
            } else {
                for (Commentaire comm : commentaires) {
                    afficherCommentaire(comm);
                }
            }
        }
    }

    private static void ajouterCommentaire() {
        System.out.println("\n--- AJOUTER UN COMMENTAIRE ---");

        System.out.print("ID du post: ");
        int id_post = scanner.nextInt();
        scanner.nextLine();

        System.out.print("ID de l'auteur: ");
        int id_auteur = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Contenu: ");
        String contenu = scanner.nextLine();

        Commentaire commentaire = new Commentaire(id_post, id_auteur, contenu);

        if (commentaireService.addCommentaire(commentaire)) {
            System.out.println("✅ Commentaire ajouté avec succès!");
        }
    }

    private static void likerCommentaire() {
        System.out.print("\nID du commentaire à liker: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        commentaireService.likeCommentaire(id);
    }

    private static void afficherPost(Post post) {
        System.out.println("\n┌─────────────────────────────────────────");
        System.out.println("│ ID: " + post.getId_post());
        System.out.println("│ Titre: " + post.getTitre());
        System.out.println("│ Auteur ID: " + post.getId_auteur());
        System.out.println("│ Catégorie: " + post.getCategorie());
        System.out.println("│ Likes: ❤️ " + post.getNb_likes());
        System.out.println("│ Date: " + post.getDate());
        System.out.println("│ Contenu: " + post.getContenu());
        System.out.println("└─────────────────────────────────────────");
    }

    private static void afficherCommentaire(Commentaire comm) {
        System.out.println("  ├─ [" + comm.getId_comment() + "] Auteur " + comm.getId_auteur() +
                " | ❤️ " + comm.getNb_likes());
        System.out.println("  │  " + comm.getContenu());
        System.out.println("  │  " + comm.getDate());
    }
}