import os
import random
import pygame
from menu import Menu
from game import Game

from constantes import SCREEN_WIDTH, SCREEN_HEIGHT

from paths import SOUNDS

pygame.init()
pygame.mixer.init()

screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))
pygame.display.set_caption("Ball Blast")

# Musique du menu
music_file = os.path.join(SOUNDS, f"music{random.randint(1, 3)}.mp3")
pygame.mixer.music.load(music_file)
pygame.mixer.music.play(-1)

menu = Menu(screen)
game = Game(screen)

state = "menu"  # menu / game / credits

clock = pygame.time.Clock()

while True:
    events = pygame.event.get()
    for event in events:
        if event.type == pygame.QUIT:
            pygame.quit()
            exit(0)

    if state == "menu":
        goToGame, newGame, credits = menu.showMenu(events)
        if credits:
            state = "credits"
        elif goToGame:
            state = "game"
            if newGame:
                game = Game(screen)

    elif state == "credits":
        if not menu.showCredits():
            state = "menu"

    elif state == "game":
        backToMenu, gameOver = game.showGame()
        if backToMenu:
            state = "menu"
        if gameOver:
            game = Game(screen)

    pygame.display.flip()
    clock.tick(60)
