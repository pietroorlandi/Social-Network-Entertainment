# Social-Network-Entertainment
Questa applicazione Android vuole rappresentare un social network basato su titoli di intrattenimento.</br>
Si appoggia alle funzionalità di Firestore Database e a Firebase Auth per l'autenticazione degli utenti.

##### Lingue disponibili
- Inglese
- Italiano

### Autenticazione
L'utente può accedere alla piattaforma tramite email e password oppure tramite l'accesso con account Google. Nel caso si scelga come metodo d'accesso l'email, bisognerà verificarla, cliccando sul token che verrà inviato (controllare nella sezione spam).
<p align="center">
<img src="https://github.com/pietroorlandi/Social-Network-Entertainment/blob/master/img/screen_access.jpeg" width="180">
</p>

### Sezioni
Quando l'utente accede correttamente alla piattaforma potrà navigare tra le cinque sezioni dell'app.

#### Home
In questa sezione saranno mostrati i titoli di intrattenimento che il sistema consiglia all'utente. È stato implementato un sistema di raccomandazione che si basa su tre parametri:
- titoli che sono stati consumati da profili seguiti dall'utente loggato
- titoli che hanno la categoria in comune con quelli già consumati dall'utente loggato
- titoli con più recensioni all'interno del database (che saranno i titoli più popolari)
<p align="center">
<img src="https://github.com/pietroorlandi/Social-Network-Entertainment/blob/master/img/screen_home.jpeg" width="180">
</p>

#### Esplora
In questa sezione l'utente potrà cercare i vari titoli di intrattenimento del database, utilizzando una barra di ricerca, oppure filtrandoli in base al tipo, all'anno di uscita oppure in base alla categoria a cui appartengono

#### Cerca utenti
In questa sezione all'utente loggato comparirà la lista di profili presenti nel database. Potrà utilizzare una barra di ricerca per cercare i profili grazie all'username. Cliccando sul profilo, apparirà il profilo dell'utente
<p align="center">
<img src="https://github.com/pietroorlandi/Social-Network-Entertainment/blob/master/img/screen_search_user.jpeg" width="180">
</p>

#### Raccomandazioni
Ogni utente può inviare delle raccomandazioni a ciascun altro utente. In questa sezione saranno mostrate tutte le raccomandazioni ricevute dall'utente, ordinandole in ordine cronologico inverso.
<p align="center">
<img src="https://github.com/pietroorlandi/Social-Network-Entertainment/blob/master/img/screen_reccomendation.jpeg" width="180">
</p>

#### Mio profilo
In questa sezione l'utente loggato può vedere e modificare tutte le informazioni relative al proprio profilo.
<p align="center">
<img src="https://github.com/pietroorlandi/Social-Network-Entertainment/blob/master/img/screen_my_profile.jpeg" width="180">
</p>

### Altre funzionalità
- l'utente aggiungerà i titoli di intrattenimento alla propria lista di intrattenimenti consumati;
- l'utente potrà seguire altri profili;
- l'utente potrà visualizzare il profilo di altri utenti, visualizzando tutti i contenuti consumati da quest'ultimo;
- l'utente potrà visualizzare tutte le informazioni riguardo un determinato titolo di intrattenimento;
- l'utente potrà recensire un determinato titolo di intrattenimento, inserendo un voto obbligatorio (da 0 a 5) e un parere testuale facoltativo;
- l'utente potrà vedere la lista dei follower e la lista dei profili seguiti di ciascun altro utente;





