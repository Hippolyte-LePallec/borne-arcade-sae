import os
import pygame
import random
from ball import Ball
from bullet import Bullet
from player import Player
from constantes import (
    WHITE, BLACK, RED, GREEN, BLUE,
    SCREEN_WIDTH, SCREEN_HEIGHT, FONT,
    FIRERATE, BALL_EQUIVALENT, FONT_SCORE
)
from paths import IMAGES, SOUNDS, EXPLOSION, HIGHSCORE


class Game():
    def __init__(self, screen: pygame.Surface):
        self.screen = screen
        self.level = 0
        self.player = Player()


        self.ball_level = [[BLACK, 50], [RED, 40], [GREEN, 33], [BLUE, 25]]
        self.ballEquivalents = [10, 7, 3, 1]
        self.ballsToSpawn = BALL_EQUIVALENT

        self.perdu = False
        self.shootCD = 0

        self.bg_path = os.path.join(IMAGES, "bg_pxl.jpg")
        self.texture = pygame.transform.scale(
            pygame.image.load(self.bg_path).convert(),
            (SCREEN_WIDTH, SCREEN_HEIGHT)
        )

        self.frameNumberLoseAnim = 0
        self.frameNumberWinAnim = 0
        self.frameNumberSpawnBalls = 0
        self.frameNumberBeginLevel = 0

        self.path = os.path.join(EXPLOSION, "frame-")

        self.player = Player()
        wheels = self.player.getWheels()

        self.playerGroup = pygame.sprite.Group()
        self.playerGroup.add(self.player)

        self.balls = pygame.sprite.Group()
        self.bullets = pygame.sprite.Group()
        self.all_sprites = pygame.sprite.Group()

        self.all_sprites.add(self.playerGroup)
        self.all_sprites.add(wheels[0])
        self.all_sprites.add(wheels[1])

    def createBalls(self):
        while True:
            ballType = random.randint(0, len(self.ball_level) - 1)
            if self.ballEquivalents[ballType] <= self.ballsToSpawn:
                newball = Ball(
                    random.randint(100, SCREEN_WIDTH - 100),
                    random.randint(-100, -40),
                    self.ball_level[ballType][1],
                    ballType,
                    self.ball_level[ballType][0]
                )
                self.balls.add(newball)
                self.all_sprites.add(newball)
                self.ballsToSpawn -= self.ballEquivalents[ballType]
                return

    def nextLevel(self):
        self.level += 1
        self.ballsToSpawn = BALL_EQUIVALENT + self.level * 5
        self.frameNumberWinAnim = 0
        self.frameNumberSpawnBalls = 0
        self.frameNumberBeginLevel = 0

    def showGame(self):
        if self.frameNumberBeginLevel < 60:
            self.frameNumberBeginLevel += 1
            self.screen.blit(self.texture, (0, 0))
            self.screen.blit(
                FONT.render('NIVEAU ' + str(self.level), True, (0, 0, 0)),
                (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2)
            )
            return False, False

        if pygame.key.get_pressed()[pygame.K_f]:
            if self.perdu:
                return True, False
            else:
                return False, True

        self.shootCD += 1

        if self.ballsToSpawn > 0:
            if self.frameNumberSpawnBalls % 20 == 0:
                self.createBalls()
            self.frameNumberSpawnBalls += 1

        if self.shootCD == FIRERATE and not self.perdu:
            self.shootCD = 0
            bullet = Bullet(self.player.rect.centerx, self.player.rect.top)
            self.all_sprites.add(bullet)
            self.bullets.add(bullet)

        self.all_sprites.update()

        hitBalls = pygame.sprite.groupcollide(self.balls, self.bullets, False, True)
        for hit in hitBalls:
            destroyed = hit.take_damage()
            if destroyed:
                self.player.score += hit.base_life_points
                if hit.level < len(self.ball_level) - 1:
                    ball1 = Ball(hit.rect.x, hit.rect.y,
                                 self.ball_level[hit.level + 1][1],
                                 hit.level + 1,
                                 self.ball_level[hit.level + 1][0])
                    ball1.decale(10)
                    self.balls.add(ball1)
                    self.all_sprites.add(ball1)

                    ball2 = Ball(hit.rect.x, hit.rect.y,
                                 self.ball_level[hit.level + 1][1],
                                 hit.level + 1,
                                 self.ball_level[hit.level + 1][0])
                    ball2.decale(-10)
                    self.balls.add(ball2)
                    self.all_sprites.add(ball2)

                hit.kill()

        self.screen.blit(self.texture, (0, 0))
        self.all_sprites.draw(self.screen)

        self.score_box = pygame.Surface((150, 50), pygame.SRCALPHA)
        pygame.draw.rect(self.score_box, (255, 255, 255, 180), self.score_box.get_rect())
        self.score_texte = FONT_SCORE.render("Score : " + str(self.player.score), True, (0, 0, 0))
        self.score_box.blit(self.score_texte, (10, 10))
        self.screen.blit(self.score_box, (10, 10))

        hitPlayer = pygame.sprite.groupcollide(self.balls, self.playerGroup, False, False)
        if hitPlayer:
            self.perdu = True
            self.player.kill()

        if self.perdu:
            self.screen.blit(FONT.render('PERDUUUUUUU', False, (0, 0, 0)),
                             (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2))

            if self.frameNumberLoseAnim == 0:
                pygame.mixer.music.load(os.path.join(SOUNDS, "musicdeath.mp3"))
                pygame.mixer.music.play()

            if self.frameNumberLoseAnim < 17:
                self.frameNumberLoseAnim += 1
                if self.frameNumberLoseAnim <= 9:
                    deathImage = pygame.image.load(self.path + "0" + str(self.frameNumberLoseAnim) + ".png")
                else:
                    deathImage = pygame.image.load(self.path + str(self.frameNumberLoseAnim) + ".png")

                self.screen.blit(deathImage, (self.player.rect.left - 20, self.player.rect.top - 80))

        if len(self.balls.sprites()) == 0 and not self.perdu:
            self.screen.blit(FONT.render('GAGNÉ', False, (0, 0, 0)),
                             (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2))

            if self.frameNumberWinAnim == 240 and not self.perdu:
                self.nextLevel()
            self.frameNumberWinAnim += 1

        return False, False

    def registerScore(self):
        alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        pseudo_chars = [0, 0, 0]
        current_position = 0
        input_active = True
        cursor_visible = True
        cursor_timer = 0

        while input_active:
            cursor_timer += 1
            if cursor_timer >= 20:
                cursor_visible = not cursor_visible
                cursor_timer = 0

            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    input_active = False
                    pygame.quit()
                    break

                if event.type == pygame.KEYDOWN:
                    if event.key == pygame.K_r:
                        pseudo = ''.join([alphabet[i] for i in pseudo_chars])
                        self._saveScore(pseudo)
                        input_active = False
                    elif event.key == pygame.K_f:
                        input_active = False
                    elif event.key == pygame.K_UP:
                        pseudo_chars[current_position] = (pseudo_chars[current_position] + 1) % len(alphabet)
                    elif event.key == pygame.K_DOWN:
                        pseudo_chars[current_position] = (pseudo_chars[current_position] - 1) % len(alphabet)
                    elif event.key == pygame.K_LEFT:
                        current_position = (current_position - 1) % 3
                    elif event.key == pygame.K_RIGHT:
                        current_position = (current_position + 1) % 3

            self.screen.blit(self.texture, (0, 0))
            self.screen.blit(FONT.render("ENREGISTRER LE SCORE !", True, WHITE),
                             (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 - 100))
            self.screen.blit(FONT.render(f"Score: {self.player.score}", True, WHITE),
                             (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 - 60))
            self.screen.blit(FONT_SCORE.render("Entrez votre pseudo (3 lettres):", True, WHITE),
                             (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 - 20))

            char_spacing = 60
            start_x = SCREEN_WIDTH // 2 - char_spacing

            for i in range(3):
                char_box = pygame.Surface((50, 50), pygame.SRCALPHA)
                if i == current_position:
                    pygame.draw.rect(char_box, (255, 255, 0, 200), char_box.get_rect())
                    pygame.draw.rect(char_box, BLACK, char_box.get_rect(), 3)
                else:
                    pygame.draw.rect(char_box, (255, 255, 255, 200), char_box.get_rect())
                    pygame.draw.rect(char_box, BLACK, char_box.get_rect(), 2)

                letter = alphabet[pseudo_chars[i]]
                char_box.blit(FONT.render(letter, True, BLACK), (12, 10))

                box_rect = char_box.get_rect(center=(start_x + i * char_spacing, SCREEN_HEIGHT // 2 + 20))
                self.screen.blit(char_box, box_rect)

                if i == current_position and cursor_visible:
                    cursor_y = SCREEN_HEIGHT // 2 + 50
                    pygame.draw.line(self.screen, WHITE,
                                     (start_x + i * char_spacing - 15, cursor_y),
                                     (start_x + i * char_spacing + 15, cursor_y), 3)

            controls_text = [
                "↑↓ : Changer la lettre",
                "←→ : Changer de position",
                "R : Valider",
                "F : Annuler"
            ]
            for j, text in enumerate(controls_text):
                self.screen.blit(FONT_SCORE.render(text, True, WHITE),
                                 (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 + 90 + j * 20))

            pygame.display.flip()
            pygame.time.Clock().tick(40)

        return False, False

    def _saveScore(self, pseudo):
        try:
            scores = []
            try:
                with open(HIGHSCORE, "r", encoding="utf-8") as file:
                    for line in file:
                        line = line.strip()
                        parts = line.split('-')
                        pseudo_existing = parts[0]
                        score_existing = int(parts[1])
                        scores.append((pseudo_existing, score_existing))
            except FileNotFoundError:
                pass

            scores.append((pseudo, self.player.score))
            scores.sort(key=lambda x: x[1], reverse=True)

            with open(HIGHSCORE, "w", encoding="utf-8") as file:
                for pseudo_score, score in scores:
                    file.write(f"{pseudo_score}-{score}\n")

        except Exception as e:
            print(f"Erreur lors de la sauvegarde du score: {e}")
