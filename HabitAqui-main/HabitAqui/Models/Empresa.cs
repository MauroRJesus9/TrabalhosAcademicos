using System.ComponentModel.DataAnnotations;

namespace HabitAqui.Models
{
    public class Empresa
    {
        public int Id { get; set; }

        [Display(Name = "Nome da Empresa")]
        [Required(ErrorMessage = "É necessário escrever o nome da Empresa")]
        public string Nome { get; set; }

        [Display(Name = "Avaliação")]
        [Required(ErrorMessage = "É necessário dar uma avaliação à Empresa")]
        public double Avaliacao { get; set; }

        public ICollection<Habitacoes>? Habitacoes { get; set; }
        public ICollection<Utilizadores>? Utilizadores { get; set; }
        public ICollection<Arrendamentos>? Arrendados { get; set; }

        //se a empresa estiver ativa ou não
        public bool Ativo { get; set; }


    }
}
