package net.dnadas.training_portal.model.group.project.questionnaire;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Question {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 3000, nullable = false)
  private String text;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private QuestionType type;

  @Column(nullable = false)
  @Min(1)
  private Integer questionOrder;

  @Column(nullable = false)
  @Min(1)
  private Integer points;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "questionnaire_id", nullable = false)
  private Questionnaire questionnaire;

  @OneToMany(mappedBy = "question", orphanRemoval = true, cascade = CascadeType.ALL,
    fetch = FetchType.EAGER)
  @OrderBy("answerOrder")
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private List<Answer> answers = new ArrayList<>();

  public Question(
    String text, QuestionType type, Integer order, Integer points, Questionnaire questionnaire) {
    this.text = text;
    this.type = type;
    this.questionOrder = order;
    this.points = points;
    this.questionnaire = questionnaire;
  }

  public List<Answer> getAnswers() {
    return List.copyOf(answers);
  }

  public void addAnswer(Answer answer) {
    this.answers.add(answer);
  }
}
