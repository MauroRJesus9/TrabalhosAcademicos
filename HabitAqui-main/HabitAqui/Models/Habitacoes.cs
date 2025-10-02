using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HabitAqui.Models{
    public class Habitacoes {

        public int Id { get; set; }

        [Required]
        [Display(Name = "Nome da Habitação")]
        [StringLength(250, MinimumLength = 3, ErrorMessage = "Max 250 chars min 3")]
        public string Nome { get; set; }

        [Required]
        [Display(Name = "Descriçao da Habitação")]
        [StringLength(5000, MinimumLength = 10, ErrorMessage = "Max 5000 chars min 10")]
        public string Descricao { get; set; }


        [Required]
        [DataType(DataType.Date)]
        [Display(Name = "Disponivel desde")]
        public DateTime DataInicio { get; set; }

        [Required]
        [DataType(DataType.Date)]
        [Display(Name = "Disponivel ate")]
        public DateTime DataFim { get; set; }

        [Required]
        [Display(Name = "Localidade")]
        [StringLength(5000, MinimumLength = 5, ErrorMessage = "Max 5000 chars min 5")]
        public string Localidade { get; set; }


        [Required]
        [Display(Name = "Preço por noite")]
        public double Preco { get; set; }


        public int? CategoriaId { get; set; }
        public Categorias? Categoria { get; set; }

        [Required]
        [Display(Name = "Periodo Minimo de Arrendamento")]
        public int PeriodoMinimoArrendamento { get; set; }

        public string Estado { get; set; }

        // Uma Habitação pode ter varias propertyimages
        public virtual ICollection<HabitacoesFotos>? Fotos { get; set; }

        // Um accomodation tem varias avaliações
        public virtual ICollection<HabitacoesRating>? Ratings { get; set; }

        // um accomodation pode ter varios arrendamentos
        public virtual ICollection<Arrendamentos>? Arrendamentos { get; set; }

        public bool Ativo { get; set; }

        //A habitação pertence a uma empresa
        public int? EmpresaId { get; set; }
        public Empresa? Empresa { get; set; }

        public string? FotografiaCaminho { get; set; }
        [NotMapped]
        public IFormFile Fotografia { get; set; }

        internal static Task AddAsync(Habitacoes habitacao)
        {
            throw new NotImplementedException();
        }
    }
}
