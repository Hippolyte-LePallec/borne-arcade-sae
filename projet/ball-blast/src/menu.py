import os
import pygame
from constantes import WHITE, SCREEN_WIDTH, SCREEN_HEIGHT, FONT
from paths import IMAGES


class Menu():
    def __init__(self, screen: pygame.Surface):
        self.screen = screen
        self.selectedOption = 0

        bg_path = os.path.join(IMAGES, "bg_pxl.jpg")
        self.texture = pygame.transform.scale(
            pygame.image.load(bg_path).convert(),
            (SCREEN_WIDTH, SCREEN_HEIGHT)
        )

    def showMenu(self, keyEvent, pause: bool = False):
        newGame = False
        credits = False
        numberOfOptions = 3 if pause else 2

        goTogame = False
        for event in keyEvent:
            if event.type == pygame.KEYDOWN:
                if event.key == pygame.K_DOWN:
                    self.selectedOption = (self.selectedOption + 1) % (numberOfOptions + 1)
                if event.key == pygame.K_UP:
                    self.selectedOption = (self.selectedOption - 1) % (numberOfOptions + 1)

                if event.key == pygame.K_r:
                    if pause:
                        if self.selectedOption == 0:
                            goTogame = True
                        elif self.selectedOption == 1:
                            newGame = True
                            goTogame = True
                        elif self.selectedOption == 2:
                            credits = True
                        elif self.selectedOption == 3:
                            pygame.quit()
                            exit(0)
                    else:
                        if self.selectedOption == 0:
                            goTogame = True
                            newGame = True
                        elif self.selectedOption == 1:
                            credits = True
                        elif self.selectedOption == 2:
                            pygame.quit()
                            exit(0)

                if event.key == pygame.K_q:
                    pygame.quit()
                    exit(0)

        self.screen.blit(self.texture, (0, 0))

        delta = 100 if pause else 0

        if pause:
            self.screen.blit(FONT.render('REPRENDRE', False, (0, 0, 0)), (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2))
            self.screen.blit(FONT.render('NOUVELLE PARTIE', False, (0, 0, 0)), (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 + 100))
        else:
            self.screen.blit(FONT.render('COMMENCER', False, (0, 0, 0)), (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2))

        self.screen.blit(FONT.render('BALL BLAST', False, (0, 0, 0)), (SCREEN_WIDTH // 2, (SCREEN_HEIGHT // 2) - 200))
        self.screen.blit(FONT.render('CRÉDITS', False, (0, 0, 0)), (SCREEN_WIDTH // 2, (SCREEN_HEIGHT // 2) + 100 + delta))
        self.screen.blit(FONT.render('QUITTER', False, (0, 0, 0)), (SCREEN_WIDTH // 2, (SCREEN_HEIGHT // 2) + 200 + delta))

        pygame.draw.circle(self.screen, WHITE,
                           ((SCREEN_WIDTH // 2) - 50, (SCREEN_HEIGHT // 2) + 100 * self.selectedOption + 25), 5)

        return goTogame, newGame, credits

    def showCredits(self):
        self.screen.blit(self.texture, (0, 0))

        self.screen.blit(FONT.render('PRODUIT PAR:', False, (0, 0, 0)),
                         (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 - 50))
        self.screen.blit(FONT.render('Justin FONTAINE', False, (0, 0, 0)),
                         (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 + 20))
        self.screen.blit(FONT.render('Arnaud WISSOCQ', False, (0, 0, 0)),
                         (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 + 70))
        self.screen.blit(FONT.render('Appuyez sur Q pour revenir', False, (0, 0, 0)),
                         (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 + 150))

        if pygame.key.get_pressed()[pygame.K_q]:
            return False
        return True
