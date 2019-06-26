(ns colorz.core
    (:require
      [reagent.core :as r]))

(def title "London Clojure Dojo, June 25th 2019")
(def prompt "Click the colors at the bottom, until you fill the screen!")

(def colors ["#66c2a5" "#fc8d62" "#8da0cb" "#e78ac3" "#a6d854" "#ffd92f"])

(defn init-board [w h]
  (into []
    (mapv vec
      (partition w
        (take (* w h) (repeatedly #(rand-int (count colors))))))))

(def app-state (r/atom {
  :board (init-board 12 12)
  :moves 0}))

(defn cell [x y side color key on-click]
 [:rect {:width side
         :height side
         :x x
         :y y
         :fill (colors color)
         :key key
         :on-click #(on-click color)}])

(defn board [cells cell-side cell-border on-click]
  (let [nrows (count cells)
        ncols (count (nth cells 0))
        cellw (- 1 cell-border)]
  [:svg {:view-box [0 0 ncols nrows]
         :width (* ncols cell-side)
         :height (* nrows cell-side)}
    (into
      (for [j (range nrows)
            i (range ncols)]
            (cell i
                  j
                  cellw
                  (get-in cells [j i])
                  (+ i (* j ncols))
                  on-click)))]))

(defn paint-board [cells color old-color x y]
  (if (or (== y (count cells))
          (== x (count (nth cells 0)))
          (not= old-color (get-in cells [x y])))
      cells
      (assoc-in
        (paint-board
          (paint-board cells color old-color x (inc y))
          color
          old-color
          (inc x)
          y)
        [x y]
        color)))

(defn click-color [color]
  (let [{moves :moves
         board :board} @app-state
        old-color (get-in board [0 0])]
    (swap! app-state assoc
      :moves (inc moves)
      :board (paint-board board color old-color 0 0))))

(defn home-page []
  [:center
    [:h3 title]
    [:h4 prompt]
    [:div (board (@app-state :board) 30 0 identity)]
    [:div "Moves: " (str (@app-state :moves))]
    [:div (board [[0 1 2 3 4 5]] 50 0.1 click-color)]])

(defn mount-root []
  (r/render-component [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
