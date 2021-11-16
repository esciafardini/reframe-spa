(ns novo-challenge.views
  (:require
   [re-frame.core :as rf]
   [novo-challenge.subs :as subs]
   [novo-challenge.events :as events]
   [novo-challenge.utils :as utils]))

(defn header
  "Displays the main page title."
  [str]
  [:h1.title str])

(defn footer
  [str]
  [:h1.footer str])

(defn title-display
  "Displays a movie title."
  [title]
  [:h2.movie-title title])

(defn date-display
  "Displays a formatted release date."
  [date]
  [:h3.date "Released: " (utils/format-date date)])

(defn loading-indicator
  "Displays a Bootstrap loading indicator."
  []
  [:div.spinner-grow {:role "status"}])

(defn character-id-list
  "Returns an unordered list element populated with data for characters."
  [id-list]
  (let [list-item-vector
        (doall
         (for [[id name] id-list]
           (into []
                 [:li.character-name
                  {:style {:cursor "pointer"}
                   :on-click
                   #(rf/dispatch [::events/select-character-view-id
                                  (utils/keyword->int id)])} (str "🌔  " name)])))]
    (into [:ul.list-wrapper] list-item-vector)))

(defn character-data
  "Converts ids in a character map to keywords for easy lookup."
  [id character-list]
  ((utils/int->keyword id) character-list))

(defn character-name
  "Displays a character's name."
  [name]
  [:h2.name name])

(defn info
  "Displays an item of information w/r/t a character."
  [label item]
  [:p.info label item])

(defn character-display
  "Given a map of character data, returns a component with formatted data."
  [character-map]
  (let [{:keys [name gender birth_year hair_color skin_color]} character-map]
    [:div.character-display
     [character-name name]
     [info "Birth Year: " birth_year]
     [info "Gender: " gender]
     [info "Hair Color: " hair_color]
     [info "Skin Color: " skin_color]
     [:button.btn.btn-outline-dark.btn-lg
      {:cursor "pointer"
       :on-click #(rf/dispatch [::events/return-to-home-screen])}
      "BACK TO HOME SCREEN"]]))

(defn film-display
  "Given maps of character and film data, returns a display component with film list & character list."
  [film-map character-map]
  [:div.display
   (doall
    (for [[k v] film-map
          :let [ids (:characters v)]]
      [:div.film-element {:key k}
       [title-display (:title v)]
       [date-display (:release_date v)]
       [character-id-list
        (reduce #(assoc %1 %2 (get-in character-map [%2 :name])) {} ids)]]))])

(defn main-panel
  "Main Display Component."
  []
  (let [view-id                 (rf/subscribe [::subs/view-id])
        loading-characters      (rf/subscribe [::subs/loading-characters])
        loading-films           (rf/subscribe [::subs/loading-films])
        film-map                (rf/subscribe [::subs/film-map])
        characters-map          (rf/subscribe [::subs/character-map])]
    (fn []
      (let [character-map (character-data @view-id @characters-map)]
        [:div.container
         [header "Star Wars"]
         (if (or @loading-characters @loading-films)
           [loading-indicator]
           (if (= @view-id 0)
             [film-display @film-map @characters-map]
             [character-display character-map]))
         [footer "Submitted by Edward Ciafardini"]]))))
