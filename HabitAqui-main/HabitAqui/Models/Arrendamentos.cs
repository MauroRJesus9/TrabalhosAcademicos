using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HabitAqui.Models{
    public class Arrendamentos{


        public int Id { get; set; }

        public double Custo { get; set; } //  custo total do arrendamento

        //A reserva é feita por um utilizador do tipo cliente
        public string? UtilizadoresId { get; set; }
        public Utilizadores Cliente { get; set; }


        //A reserva é processada por um utilizador do tipo funcionario da empresa
        public int? EmpresaId { get; set; }
        public Empresa Empresa { get; set; }


        //A reserva é feita para uma habitação
        public int? HabitacoesId { get; set; }
        public Habitacoes Habitacoes { get; set; }

        [Required]
        [Display(Name = "Estado da Reserva")]
        public string Estado { get; set; } // Pendente, Confirmada, Rejeitada, Concluída

        public int? EstadoInicialId { get; set; }
        //[Display(Name = "Breve resumo do estado inicial")]
        //[StringLength(100, MinimumLength = 5, ErrorMessage = "Max 100 chars min 5")]
        public EstadoHabitacao? EstadoInicial { get; set; }


        public int? EstadoFinalId { get; set; }
        //[Display(Name = "Breve Resumo do estado final")]
        //[StringLength(100, MinimumLength = 5, ErrorMessage = "Max 100 chars min 5")]
        public EstadoHabitacao? EstadoFinal { get; set; }

        //public ICollection<HabitacoesFotos>? AfterFotos{ get; set; }

        public string? CaminhoImagem { get; set; }
        [NotMapped]
        public IFormFile? ImagemDanos { get; set; }


        [Required]
        [DataType(DataType.Date)]
        [Display(Name = "Arrendado desde:")]
        public DateTime DataInicio { get; set; }

        [Required]
        [DataType(DataType.Date)]
        [Display(Name = "Arrendado ate:")]
        public DateTime DataFim { get; set; }


    }
}
