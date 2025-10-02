using HabitAqui.Models;
using System.ComponentModel.DataAnnotations;

namespace HabitAqui.ViewModels.Clientes
{
    public class ClienteViewModel{
        public Habitacoes Habitacao { get; set; }

        public List<Habitacoes> habitacoes { get; set; }

        public Arrendamentos Arrendamento { get; set; }

        public List<Arrendamentos> Arrendamentos { get; set; }
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

        public string Id { get; set; }
        public string Username { get; set; }
        public bool Ativo { get; set; }
        public string Role { get; set; }
    }
}
