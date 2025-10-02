using HabitAqui.Models;
using System.ComponentModel.DataAnnotations;

namespace HabitAqui.ViewModels.UsersAnonimos
{
    public class UserAnonimoViewModel{

        public Habitacoes Habitacao { get; set; }

        public List<Habitacoes> habitacoes { get; set; }

        //filtros
        public string? CategoriaFiltro { get; set; }
        public string? EstadoFiltro { get; set; }
        public string? LocalidadeFiltro { get; set; }
        public string? PeriodoMinimoArrendamentoFiltro { get; set; }
        

        //ordenação
        public string? OrdenarPor { get; set; }
        public bool OrdemAscendente { get; set; }

        // Para exibir mensagens de sucesso/erro
        public string MensagemFeedback { get; set; }

    }
}
