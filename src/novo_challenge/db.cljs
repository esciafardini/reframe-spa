(ns novo-challenge.db)

(def default-db
  {:loading-characters     true
   :loading-films          true
   :last-error             nil
   :view-id                0
   :uri                    "https://swapi.dev/api/people"
   :film-data              {}
   :character-data         nil
   })
