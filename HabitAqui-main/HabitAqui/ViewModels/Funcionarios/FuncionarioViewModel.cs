using System.ComponentModel.DataAnnotations;

namespace HabitAqui.ViewModels.Funcionarios
{
    public class FuncionarioViewModel{

        public string Id { get; set; }
        public string Username { get; set; }

        [Display(Name = "Reservas")]
        public int NrArrendamentos { get; set; }
        public bool Ativo { get; set; }
        public string Role { get; set; }

    }
}
