using System.ComponentModel.DataAnnotations;

namespace HabitAqui.Models
{
    public class Categorias{

        public int Id { get; set; }

        [Display(Name = "Categoria", Prompt = "Introduza a Categoria!")]
        [Required(ErrorMessage = "Indique o nome da Categoria!")]
        public string Nome { get; set; }

        [Display(Name = "Disponível?")]
        public bool Disponivel { get; set; }

    }
}
