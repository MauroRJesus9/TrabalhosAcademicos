using System.ComponentModel.DataAnnotations;

namespace HabitAqui.Models
{
    public class HabitacoesRating
    {

        public int ID { get; set; }

        [Required]
        [Display(Name = "Comenta sobre a habitação")]
        [StringLength(5000, MinimumLength = 10, ErrorMessage = "Max 5000 chars min 10")]
        public string Comentario { get; set; }

        // Uma avaliação pertence a uma Habitação
        public int? HabitacoesId { get; set; }
        public Habitacoes Habitacoes { get; set; }

        // uma avaliação é dada por um utilizador
        public string? UtilizadoresId { get; set; }
        public Utilizadores Utilizador { get; set; }

    }
}
