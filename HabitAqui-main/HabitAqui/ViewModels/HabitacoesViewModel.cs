using HabitAqui.Models;
using System.ComponentModel.DataAnnotations;

namespace HabitAqui.ViewModels
{
    public class HabitacoesViewModel
    {
        public List<Habitacoes>? Habitacoes { get; set; }

        public int NumeroHabitacoes { get; set; }

        public string? TextoPesquisa { get; set; }

    }
}
