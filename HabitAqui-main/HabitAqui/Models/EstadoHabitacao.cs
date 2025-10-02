using System.ComponentModel.DataAnnotations;

namespace HabitAqui.Models
{
    public class EstadoHabitacao
    {

        public int Id { get; set; }

        [Display(Name = "Equipamentos Opcionais")]
        public string? EquipamentosOpcionais { get; set; }
        [Display(Name = "Danos na Habitação")]
        public Boolean Dano { get; set; }
        [Display(Name = "Observações")]
        public string? Observações { get; set; }
        [Display(Name = "ID Funcionário de Entrega")]
        // The employer that reported
        public string FuncionarioEntregaId { get; set; }
        [Display(Name = "Funcionário de Entrega")]
        public Utilizadores? FuncionarioEntrega { get; set; }

    }
}
