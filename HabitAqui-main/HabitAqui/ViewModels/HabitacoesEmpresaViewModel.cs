using HabitAqui.Models;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HabitAqui.ViewModels
{
    public class HabitacoesEmpresaViewModel{


        //Dados da habitação
        public Habitacoes Habitacao { get; set; }

        public List<Habitacoes> habitacoes { get; set; }

        //filtros
        public string? CategoriaFiltro { get; set; }
        public string? EstadoFiltro { get; set; }

        //ordenação
        public string? OrdenarPor { get; set; }
        public bool OrdemAscendente { get; set; }

        // Para exibir mensagens de sucesso/erro
        public string MensagemFeedback { get; set; }

        public string? FotografiaCaminho { get; set; }
        [NotMapped]
        public IFormFile Fotografia { get; set; }
    

    }
}
